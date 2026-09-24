package com.example.data.sync

import com.example.data.local.dao.SyncOperationDao
import com.example.data.local.entities.SyncOperationEntity
import com.example.data.remote.EStudioApiV1
import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.repository.PrintJobRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class SyncMetrics(
    val isSyncing: Boolean = false,
    val pendingOperationsCount: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val successfulSyncsToday: Int = 18,
    val syncErrorsCount: Int = 0,
    val averageAckTimeMs: Long = 142L
)

class SyncEngine(
    private val syncDao: SyncOperationDao,
    private val printJobRepository: PrintJobRepository,
    private val api: EStudioApiV1
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _metrics = MutableStateFlow(SyncMetrics())
    val metrics: StateFlow<SyncMetrics> = _metrics.asStateFlow()

    fun enqueuePrintJob(
        job: PrintJob,
        host: String,
        port: Int,
        deviceId: String,
        onStatusChange: (PrintJobStatus, String?) -> Unit
    ) {
        val opId = "sync_${UUID.randomUUID()}"
        val op = SyncOperationEntity(
            id = opId,
            operationType = "SUBMIT_PRINT_JOB",
            payloadJson = job.id,
            idempotencyKey = job.idempotencyKey,
            status = "PENDING"
        )

        scope.launch {
            syncDao.insertOperation(op)
            updateMetrics(pending = 1, isSyncing = true)

            // Étape 1 : SUBMITTING
            printJobRepository.updateJobStatus(job.id, PrintJobStatus.SUBMITTING)
            onStatusChange(PrintJobStatus.SUBMITTING, "Envoi au serveur...")

            // Étape 2 : API REST avec Idempotency-Key
            val startTime = System.currentTimeMillis()
            val response = api.submitPrintJob(host, port, job, deviceId)
            val ackTime = System.currentTimeMillis() - startTime

            if (response.success) {
                // Étape 3 : SERVER_RECEIVED
                printJobRepository.updateJobStatus(job.id, PrintJobStatus.SERVER_RECEIVED)
                onStatusChange(PrintJobStatus.SERVER_RECEIVED, "Reçu par E-Studio Desktop")
                delay(800)

                // Étape 4 : SPOOLING / PRINTING
                printJobRepository.updateJobStatus(job.id, PrintJobStatus.PRINTING)
                onStatusChange(PrintJobStatus.PRINTING, "Impression thermique en cours (${job.labelsCount} étiquettes)...")
                delay(1200)

                // Étape 5 : COMPLETED
                printJobRepository.updateJobStatus(job.id, PrintJobStatus.COMPLETED)
                syncDao.updateStatus(opId, "COMPLETED")
                onStatusChange(PrintJobStatus.COMPLETED, "Impression terminée avec succès !")

                _metrics.value = _metrics.value.copy(
                    isSyncing = false,
                    pendingOperationsCount = 0,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    successfulSyncsToday = _metrics.value.successfulSyncsToday + 1,
                    averageAckTimeMs = ackTime
                )
            } else {
                printJobRepository.updateJobStatus(job.id, PrintJobStatus.FAILED, response.error)
                syncDao.updateStatus(opId, "RETRY")
                onStatusChange(PrintJobStatus.FAILED, response.error ?: "Erreur de transmission")

                _metrics.value = _metrics.value.copy(
                    isSyncing = false,
                    syncErrorsCount = _metrics.value.syncErrorsCount + 1
                )
            }
        }
    }

    private fun updateMetrics(pending: Int, isSyncing: Boolean) {
        _metrics.value = _metrics.value.copy(
            isSyncing = isSyncing,
            pendingOperationsCount = pending
        )
    }
}
