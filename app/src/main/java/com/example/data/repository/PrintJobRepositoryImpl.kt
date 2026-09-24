package com.example.data.repository

import com.example.data.local.dao.PrintJobDao
import com.example.data.local.entities.PrintJobEntity
import com.example.domain.model.Money
import com.example.domain.model.PricingSnapshot
import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.model.PrintPriority
import com.example.domain.model.ProductSnapshot
import com.example.domain.model.ScanItem
import com.example.domain.model.ScanItemStatus
import com.example.domain.model.ScanSource
import com.example.domain.model.WorkSession
import com.example.domain.repository.PrintJobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PrintJobRepositoryImpl(private val dao: PrintJobDao) : PrintJobRepository {

    override val allJobs: Flow<List<PrintJob>> = dao.getAllPrintJobs().map { list ->
        list.map { mapEntityToJob(it) }
    }

    override suspend fun createPrintJobFromSession(
        session: WorkSession,
        templateId: String,
        templateName: String,
        printerId: String,
        storeId: String,
        priority: PrintPriority
    ): PrintJob {
        val jobId = "job_${System.currentTimeMillis()}_${(100..999).random()}"
        val idempotencyKey = "idem_print_${UUID.randomUUID()}"

        val printJob = PrintJob(
            id = jobId,
            sessionId = session.id,
            sessionName = session.name,
            templateId = templateId,
            templateName = templateName,
            printerId = printerId,
            storeId = storeId,
            operatorName = session.operatorName,
            priority = priority,
            status = PrintJobStatus.QUEUED,
            items = session.items,
            labelsCount = session.totalLabels,
            idempotencyKey = idempotencyKey,
            createdAt = System.currentTimeMillis()
        )

        dao.insertPrintJob(mapJobToEntity(printJob))
        return printJob
    }

    override suspend fun updateJobStatus(jobId: String, status: PrintJobStatus, errorMessage: String?) {
        val completedAt = if (status == PrintJobStatus.COMPLETED) System.currentTimeMillis() else null
        dao.updateJobStatus(jobId, status.name, completedAt, errorMessage)
    }

    override suspend fun getJobById(id: String): PrintJob? {
        return dao.getPrintJobById(id)?.let { mapEntityToJob(it) }
    }

    private fun mapJobToEntity(job: PrintJob): PrintJobEntity {
        val array = JSONArray()
        job.items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("barcode", item.barcode)
                put("quantity", item.quantity)
                item.productSnapshot?.let {
                    put("designation", it.designation)
                }
            }
            array.put(obj)
        }

        return PrintJobEntity(
            id = job.id,
            sessionId = job.sessionId,
            sessionName = job.sessionName,
            templateId = job.templateId,
            templateName = job.templateName,
            printerId = job.printerId,
            storeId = job.storeId,
            operatorName = job.operatorName,
            priority = job.priority.name,
            status = job.status.name,
            itemsJson = array.toString(),
            labelsCount = job.labelsCount,
            idempotencyKey = job.idempotencyKey,
            createdAt = job.createdAt,
            completedAt = job.completedAt,
            errorMessage = job.errorMessage
        )
    }

    private fun mapEntityToJob(entity: PrintJobEntity): PrintJob {
        val items = mutableListOf<ScanItem>()
        runCatching {
            val array = JSONArray(entity.itemsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val desig = obj.optString("designation")
                items.add(
                    ScanItem(
                        id = obj.getString("id"),
                        barcode = obj.getString("barcode"),
                        quantity = obj.optInt("quantity", 1),
                        productSnapshot = if (desig.isNotBlank()) {
                            ProductSnapshot(
                                sku = "SKU-${obj.getString("barcode")}",
                                barcode = obj.getString("barcode"),
                                designation = desig,
                                category = "Général",
                                department = "Rayon"
                            )
                        } else null,
                        idempotencyKey = "idem_${UUID.randomUUID()}"
                    )
                )
            }
        }

        return PrintJob(
            id = entity.id,
            sessionId = entity.sessionId,
            sessionName = entity.sessionName,
            templateId = entity.templateId,
            templateName = entity.templateName,
            printerId = entity.printerId,
            storeId = entity.storeId,
            operatorName = entity.operatorName,
            priority = runCatching { PrintPriority.valueOf(entity.priority) }.getOrDefault(PrintPriority.NORMAL),
            status = runCatching { PrintJobStatus.valueOf(entity.status) }.getOrDefault(PrintJobStatus.QUEUED),
            items = items,
            labelsCount = entity.labelsCount,
            idempotencyKey = entity.idempotencyKey,
            createdAt = entity.createdAt,
            completedAt = entity.completedAt,
            errorMessage = entity.errorMessage
        )
    }
}
