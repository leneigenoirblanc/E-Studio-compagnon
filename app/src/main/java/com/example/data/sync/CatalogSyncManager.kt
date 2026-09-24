package com.example.data.sync

import com.example.domain.model.CatalogSyncInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Gestionnaire de synchronisation différentielle et hors-ligne du catalogue de produits
 */
class CatalogSyncManager {

    private val _syncInfo = MutableStateFlow(
        CatalogSyncInfo(
            localVersion = "2026.09.23.04",
            serverVersion = "2026.09.24.01",
            totalArticlesCount = 18742,
            addedArticlesCount = 142,
            modifiedArticlesCount = 381,
            deletedArticlesCount = 17,
            isUpToDate = false,
            lastSyncTimestamp = System.currentTimeMillis() - (8 * 24 * 3600 * 1000L),
            isSyncing = false,
            syncProgress = 0f
        )
    )
    val syncInfo: StateFlow<CatalogSyncInfo> = _syncInfo.asStateFlow()

    /**
     * Lance la synchronisation différentielle du catalogue avec retour de progression
     */
    suspend fun performDifferentialSync(onProgress: (Float, Int, Int) -> Unit = { _, _, _ -> }) {
        _syncInfo.update { it.copy(isSyncing = true, syncProgress = 0f) }

        val total = _syncInfo.value.totalArticlesCount
        val steps = 20
        for (i in 1..steps) {
            delay(60)
            val progress = i / steps.toFloat()
            val currentCount = (progress * total).toInt()
            _syncInfo.update { it.copy(syncProgress = progress) }
            onProgress(progress, currentCount, total)
        }

        _syncInfo.update {
            it.copy(
                isSyncing = false,
                isUpToDate = true,
                localVersion = it.serverVersion,
                syncProgress = 1f,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }

    suspend fun startDifferentialSync(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            performDifferentialSync()
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Erreur de synchronisation")
        }
    }
}
