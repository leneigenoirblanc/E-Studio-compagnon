package com.example.data.instance

import android.content.Context
import com.example.domain.model.EStudioInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Gestionnaire des instances E-Studio Desktop/Server (Concept "AnyDesk")
 * Découverte locale mDNS/NSD, instances enregistrées et reconnexion intelligente
 */
class InstanceManager(context: Context) {

    private val prefs = context.getSharedPreferences("estudio_instances", Context.MODE_PRIVATE)

    private val _discoveredInstances = MutableStateFlow<List<EStudioInstance>>(emptyList())
    val discoveredInstances: StateFlow<List<EStudioInstance>> = _discoveredInstances.asStateFlow()

    private val _savedInstances = MutableStateFlow<List<EStudioInstance>>(emptyList())
    val savedInstances: StateFlow<List<EStudioInstance>> = _savedInstances.asStateFlow()

    private val _activeInstance = MutableStateFlow<EStudioInstance?>(null)
    val activeInstance: StateFlow<EStudioInstance?> = _activeInstance.asStateFlow()

    init {
        loadSavedInstances()
        discoverLocalNetwork()
    }

    fun refreshDiscovery() {
        discoverLocalNetwork()
    }

    fun discoverLocalNetwork() {
        // Simulation découverte réseau local (mDNS / NSD / Bonjour)
        val discovered = listOf(
            EStudioInstance(
                id = "INST-CAISSE-01",
                name = "E-STUDIO — CAISSE PRINCIPALE",
                address = "192.168.1.25",
                port = 8080,
                isOnline = true,
                isFavorite = true,
                lastConnectedAt = System.currentTimeMillis() - 3600_000L,
                catalogArticleCount = 18742,
                catalogVersion = "2026.09.24.01",
                storeName = "Magasin Central"
            ),
            EStudioInstance(
                id = "INST-BUREAU-02",
                name = "E-STUDIO — BUREAU DIRECTION",
                address = "192.168.1.30",
                port = 8080,
                isOnline = true,
                isFavorite = false,
                lastConnectedAt = System.currentTimeMillis() - 86400_000L * 2,
                catalogArticleCount = 18742,
                catalogVersion = "2026.09.24.01",
                storeName = "Magasin Central"
            ),
            EStudioInstance(
                id = "INST-ENTREPOT-03",
                name = "E-STUDIO — ENTREPÔT LOGISTIQUE",
                address = "192.168.1.42",
                port = 8080,
                isOnline = false,
                isFavorite = true,
                lastConnectedAt = System.currentTimeMillis() - 86400_000L * 5,
                catalogArticleCount = 18600,
                catalogVersion = "2026.09.20.02",
                storeName = "Plateforme Nord"
            )
        )
        _discoveredInstances.value = discovered
    }

    fun loadSavedInstances() {
        val defaultSaved = listOf(
            EStudioInstance(
                id = "INST-CAISSE-01",
                name = "Magasin Central",
                address = "192.168.1.25",
                port = 8080,
                isOnline = true,
                isFavorite = true,
                lastConnectedAt = System.currentTimeMillis() - (12 * 60 * 1000L),
                catalogArticleCount = 18742,
                catalogVersion = "2026.09.24.01",
                storeName = "Magasin Central"
            ),
            EStudioInstance(
                id = "INST-ENTREPOT-03",
                name = "Entrepôt Logistique",
                address = "192.168.1.42",
                port = 8080,
                isOnline = false,
                isFavorite = true,
                lastConnectedAt = System.currentTimeMillis() - (3 * 24 * 3600 * 1000L),
                catalogArticleCount = 18600,
                catalogVersion = "2026.09.20.02",
                storeName = "Plateforme Nord"
            ),
            EStudioInstance(
                id = "INST-BOUTIQUE-02",
                name = "Boutique 02",
                address = "192.168.1.55",
                port = 8080,
                isOnline = true,
                isFavorite = false,
                lastConnectedAt = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L),
                catalogArticleCount = 4210,
                catalogVersion = "2026.09.18.01",
                storeName = "Boutique Centre-Ville"
            )
        )
        _savedInstances.value = defaultSaved
        _activeInstance.value = defaultSaved[0]
    }

    fun connectToInstance(instance: EStudioInstance) {
        val updated = instance.copy(
            lastConnectedAt = System.currentTimeMillis(),
            isOnline = true
        )
        _activeInstance.value = updated
        _savedInstances.update { list ->
            val idx = list.indexOfFirst { it.id == instance.id }
            if (idx >= 0) {
                list.toMutableList().apply { set(idx, updated) }
            } else {
                list + updated
            }
        }
    }

    fun forgetInstance(instanceId: String) {
        _savedInstances.update { list -> list.filterNot { it.id == instanceId } }
        if (_activeInstance.value?.id == instanceId) {
            _activeInstance.value = _savedInstances.value.firstOrNull()
        }
    }

    fun toggleFavorite(instanceId: String) {
        _savedInstances.update { list ->
            list.map { if (it.id == instanceId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }
}
