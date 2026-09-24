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
    val transferredAt: String? = null
)
