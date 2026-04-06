package com.beepmetoo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.beepmetoo.data.db.entity.UptimeEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface UptimeDao {

    @Insert
    suspend fun insert(entry: UptimeEntry): Long

    @Query("SELECT * FROM uptime_entries WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestOpen(): UptimeEntry?

    @Query("UPDATE uptime_entries SET endTime = :endTime WHERE id = :id")
    suspend fun closeEntry(id: Long, endTime: Long)

    @Query("SELECT * FROM uptime_entries ORDER BY startTime DESC")
    fun getAll(): Flow<List<UptimeEntry>>
}
