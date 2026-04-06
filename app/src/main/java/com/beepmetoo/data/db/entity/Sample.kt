package com.beepmetoo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "samples")
data class Sample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val photoPath: String? = null,
    val timestamp: Long,
    val createdAt: Long,
)
