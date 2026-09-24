package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AuditEventDao
import com.example.data.local.dao.PrintJobDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SyncOperationDao
import com.example.data.local.dao.WorkSessionDao
import com.example.data.local.entities.AuditEventEntity
import com.example.data.local.entities.PrintJobEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SyncOperationEntity
import com.example.data.local.entities.WorkSessionEntity

@Database(
    entities = [
        LotEntity::class,
        ProductEntity::class,
        WorkSessionEntity::class,
        PrintJobEntity::class,
        SyncOperationEntity::class,
        AuditEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lotDao(): LotDao
    abstract fun productDao(): ProductDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun printJobDao(): PrintJobDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun auditEventDao(): AuditEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "estudio_scanner.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
