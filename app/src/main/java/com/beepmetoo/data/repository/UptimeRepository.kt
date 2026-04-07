package com.beepmetoo.data.repository

import com.beepmetoo.data.db.dao.UptimeDao
import com.beepmetoo.data.db.entity.UptimeEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UptimeRepository @Inject constructor(
    private val dao: UptimeDao,
) {
    suspend fun startSession() {
        dao.insert(UptimeEntry(startTime = System.currentTimeMillis()))
    }

    suspend fun stopSession() {
        val open = dao.getLatestOpen() ?: return
        dao.closeEntry(open.id, System.currentTimeMillis())
    }

    fun getAllEntries(): Flow<List<UptimeEntry>> =
        dao.getAll()
}
