package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_lots")
data class LotEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val operatorName: String,
    val deviceName: String,
    val deviceType: String,
    val status: String,
    val targetTemplateId: String?,
    val syncMethod: String,
    val itemsJson: String,
    val isTransferred: Boolean = false,
    val transferredAt: String? = null,
    val description: String = "",
    val version: Int = 1,
    val department: String = "Épicerie",
    val colorTag: String = "NORMAL",
    val catalogVersion: String = "2026.09.24.01",
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val duplicateRule: String = "INCREMENT_QTY",
    val requiresTemplate: Boolean = true,
    val requiresQuantity: Boolean = true
)
