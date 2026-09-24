package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {

    @Query("SELECT * FROM scan_lots WHERE isDeleted = 0 AND isTransferred = 0 ORDER BY isPinned DESC, updatedAt DESC LIMIT 1")
    fun getActiveLot(): Flow<LotEntity?>

    @Query("SELECT * FROM scan_lots WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllLots(): Flow<List<LotEntity>>

    @Query("SELECT * FROM scan_lots WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashLots(): Flow<List<LotEntity>>

    @Query("SELECT * FROM scan_lots WHERE id = :id LIMIT 1")
    suspend fun getLotById(id: String): LotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: LotEntity)

    @Update
    suspend fun updateLot(lot: LotEntity)

    @Query("UPDATE scan_lots SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteLotById(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE scan_lots SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreLotById(id: String)

    @Query("DELETE FROM scan_lots WHERE id = :id")
    suspend fun deleteLotById(id: String)

    @Query("UPDATE scan_lots SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("UPDATE scan_lots SET isLocked = :isLocked WHERE id = :id")
    suspend fun setLocked(id: String, isLocked: Boolean)

    @Query("UPDATE scan_lots SET isTransferred = 1, status = 'spooled', transferredAt = :timestamp WHERE id = :lotId")
    suspend fun markAsTransferred(lotId: String, timestamp: String)

    @Query("DELETE FROM scan_lots WHERE isTransferred = 1")
    suspend fun clearTransferredLots()

    @Query("DELETE FROM scan_lots WHERE isDeleted = 1")
    suspend fun emptyTrash()
}
