package com.beepmetoo.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_beeps",
    foreignKeys = [
        ForeignKey(
            entity = TimerProfile::class,
            parentColumns = ["id"],
            childColumns = ["timerProfileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ScheduledBeep(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduledTime: Long,
    val timerProfileId: Long,
    val fired: Boolean = false,
)
