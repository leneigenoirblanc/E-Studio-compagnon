package com.example.domain.repository

import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.model.PrintPriority
import com.example.domain.model.WorkSession
import kotlinx.coroutines.flow.Flow

interface PrintJobRepository {
    val allJobs: Flow<List<PrintJob>>
    suspend fun createPrintJobFromSession(
        session: WorkSession,
        templateId: String,
        templateName: String,
        printerId: String,
        storeId: String,
        priority: PrintPriority
    ): PrintJob
    suspend fun updateJobStatus(jobId: String, status: PrintJobStatus, errorMessage: String? = null)
    suspend fun getJobById(id: String): PrintJob?
}
