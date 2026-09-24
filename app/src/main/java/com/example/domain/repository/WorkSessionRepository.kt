package com.example.domain.repository

import com.example.domain.model.ScanItem
import com.example.domain.model.SessionStatus
import com.example.domain.model.WorkSession
import kotlinx.coroutines.flow.Flow

interface WorkSessionRepository {
    val activeSession: Flow<WorkSession?>
    val allSessions: Flow<List<WorkSession>>
    suspend fun getSessionById(id: String): WorkSession?
    suspend fun saveSession(session: WorkSession)
    suspend fun updateSessionStatus(sessionId: String, status: SessionStatus)
    suspend fun deleteSession(sessionId: String)
    suspend fun createNewSession(name: String, operatorName: String, deviceName: String): WorkSession
}
