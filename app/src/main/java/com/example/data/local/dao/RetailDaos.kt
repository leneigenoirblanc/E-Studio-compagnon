package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AuditEventEntity
import com.example.data.local.entities.PrintJobEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SyncOperationEntity
import com.example.data.local.entities.WorkSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY designation ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE designation LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY designation ASC LIMIT 50")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}

@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions WHERE status IN ('ACTIVE', 'DRAFT', 'PAUSED') ORDER BY updatedAt DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<WorkSessionEntity?>

    @Query("SELECT * FROM work_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): WorkSessionEntity?

    @Query("SELECT * FROM work_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<WorkSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSessionEntity)

    @Update
    suspend fun updateSession(session: WorkSessionEntity)

    @Query("UPDATE work_sessions SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSessionStatus(id: String, status: String, timestamp: Long)

    @Query("DELETE FROM work_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs ORDER BY createdAt DESC")
    fun getAllPrintJobs(): Flow<List<PrintJobEntity>>

    @Query("SELECT * FROM print_jobs WHERE id = :id LIMIT 1")
    suspend fun getPrintJobById(id: String): PrintJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrintJob(job: PrintJobEntity)

    @Query("UPDATE print_jobs SET status = :status, completedAt = :completedAt, errorMessage = :error WHERE id = :id")
    suspend fun updateJobStatus(id: String, status: String, completedAt: Long?, error: String?)

    @Query("SELECT COUNT(*) FROM print_jobs WHERE status = 'COMPLETED'")
    fun getCompletedJobsCount(): Flow<Int>
}

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE status IN ('PENDING', 'RETRY') ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperationEntity)

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Query("UPDATE sync_operations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM sync_operations WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}

@Dao
interface AuditEventDao {
    @Insert
    suspend fun insertAudit(event: AuditEventEntity)

    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAudits(): Flow<List<AuditEventEntity>>
}
