package com.beepmetoo.data.repository

import com.beepmetoo.data.db.dao.TimerProfileDao
import com.beepmetoo.data.db.entity.TimerProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerProfileRepository @Inject constructor(
    private val dao: TimerProfileDao,
) {
    fun getActiveProfile(): Flow<TimerProfile?> =
        dao.getActive()

    suspend fun setActiveProfile(id: Long) =
        dao.setActive(id)

    suspend fun createProfile(profile: TimerProfile): Long =
        dao.insert(profile)

    suspend fun updateProfile(profile: TimerProfile) =
        dao.update(profile)

    suspend fun deleteProfile(profile: TimerProfile) =
        dao.delete(profile)
}
