package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val barcode: String,
    val sku: String,
    val designation: String,
    val category: String,
    val department: String,
    val regularPriceMinor: Long,
    val promoPriceMinor: Long? = null,
    val currency: String = "EUR",
    val promotionId: String? = null,
    val facing: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_sessions")
data class WorkSessionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sessionType: String,
    val operatorId: String,
    val operatorName: String,
    val deviceId: String,
    val deviceName: String,
    val status: String,
    val targetTemplateId: String?,
    val targetPrinterId: String,
    val priority: String,
    val itemsJson: String, // Stockage JSON sérialisé des ScanItems
    val idempotencyKey: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "print_jobs")
data class PrintJobEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val sessionName: String,
    val templateId: String,
    val templateName: String,
    val printerId: String,
    val storeId: String,
    val operatorName: String,
    val priority: String,
    val status: String, // QUEUED, SUBMITTING, SERVER_RECEIVED, SPOOLING, PRINTING, COMPLETED, FAILED
    val itemsJson: String,
    val labelsCount: Int,
    val idempotencyKey: String,
    val createdAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey
    val id: String,
    val operationType: String, // "SUBMIT_PRINT_JOB", "SYNC_SESSION", "AUDIT_LOG"
    val payloadJson: String,
    val idempotencyKey: String,
    val status: String, // PENDING, UPLOADING, ACCEPTED, RETRY, FAILED, COMPLETED
    val retryCount: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val nextRetryAt: Long = 0L
)

@Entity(tableName = "audit_events")
data class AuditEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val barcode: String?,
    val operatorName: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
