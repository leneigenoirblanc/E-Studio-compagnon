package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {

    @Query("SELECT * FROM scan_lots WHERE isTransferred = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getActiveLot(): Flow<LotEntity?>

    @Query("SELECT * FROM scan_lots ORDER BY updatedAt DESC")
    fun getAllLots(): Flow<List<LotEntity>>

    @Query("SELECT * FROM scan_lots WHERE id = :id LIMIT 1")
    suspend fun getLotById(id: String): LotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: LotEntity)

    @Update
    suspend fun updateLot(lot: LotEntity)

    @Query("DELETE FROM scan_lots WHERE id = :id")
    suspend fun deleteLotById(id: String)

    @Query("UPDATE scan_lots SET isTransferred = 1, status = 'spooled', transferredAt = :timestamp WHERE id = :lotId")
    suspend fun markAsTransferred(lotId: String, timestamp: String)

    @Query("DELETE FROM scan_lots WHERE isTransferred = 1")
    suspend fun clearTransferredLots()
}
