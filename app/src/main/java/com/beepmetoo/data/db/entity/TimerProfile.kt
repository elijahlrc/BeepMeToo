package com.beepmetoo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_profiles")
data class TimerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = false,
    val beepsPerDay: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
)
