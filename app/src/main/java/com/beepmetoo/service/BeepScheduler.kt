package com.beepmetoo.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.beepmetoo.data.db.dao.ScheduledBeepDao
import com.beepmetoo.data.db.entity.ScheduledBeep
import com.beepmetoo.data.db.entity.TimerProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BeepScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduledBeepDao: ScheduledBeepDao,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    /**
     * Schedule all beeps for a given profile for today (or a specific date).
     * Persists them to the database and sets exact alarms.
     */
    suspend fun scheduleBeepsForDate(
        profile: TimerProfile,
        date: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ) {
        val allTimes = computeBeepTimes(profile, date, zone)
        val futureTimes = filterFutureBeeps(allTimes, System.currentTimeMillis())

        for (time in futureTimes) {
            val beepId = scheduledBeepDao.insert(
                ScheduledBeep(scheduledTime = time, timerProfileId = profile.id)
            )
            setExactAlarm(beepId, time)
        }
    }

    /**
     * Cancel all pending alarms for a profile and remove unfired beeps from DB.
     */
    suspend fun cancelBeepsForProfile(profileId: Long) {
        scheduledBeepDao.deleteByProfile(profileId)
        // Note: individual alarms are identified by beep ID in PendingIntent.
        // Deleted DB rows won't fire because BeepReceiver checks the DB.
    }

    /**
     * Reschedule all unfired beeps (e.g., after boot). Reads from DB and sets alarms.
     */
    suspend fun rescheduleUnfiredBeeps(unfiredBeeps: List<ScheduledBeep>) {
        val now = System.currentTimeMillis()
        for (beep in unfiredBeeps) {
            if (beep.scheduledTime > now) {
                setExactAlarm(beep.id, beep.scheduledTime)
            } else {
                // Past beep that was missed — mark as fired
                scheduledBeepDao.markFired(beep.id)
            }
        }
    }

    private fun setExactAlarm(beepId: Long, triggerAtMillis: Long) {
        val intent = Intent(context, BeepReceiver::class.java).apply {
            action = ACTION_BEEP
            putExtra(EXTRA_BEEP_ID, beepId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            beepId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback: inexact alarm if user hasn't granted exact alarm permission
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        }
    }

    companion object {
        const val ACTION_BEEP = "com.beepmetoo.ACTION_BEEP"
        const val EXTRA_BEEP_ID = "beep_id"

        /**
         * Pure function: compute random beep times for a profile on a given date.
         *
         * Algorithm: divide the active window into N equal slots, pick one
         * random time within each slot. Returns sorted epoch-ms timestamps.
         */
        fun computeBeepTimes(
            profile: TimerProfile,
            date: LocalDate,
            zone: ZoneId,
        ): List<Long> {
            val windowStart = date.atTime(LocalTime.of(profile.startHour, profile.startMinute))
                .atZone(zone).toInstant().toEpochMilli()
            val windowEnd = date.atTime(LocalTime.of(profile.endHour, profile.endMinute))
                .atZone(zone).toInstant().toEpochMilli()

            val windowDuration = windowEnd - windowStart
            if (windowDuration <= 0 || profile.beepsPerDay <= 0) return emptyList()

            val slotDuration = windowDuration / profile.beepsPerDay

            return (0 until profile.beepsPerDay).map { slot ->
                val slotStart = windowStart + (slot * slotDuration)
                slotStart + Random.nextLong(slotDuration)
            }.sorted()
        }

        /**
         * Filter out beep times that are in the past relative to [now].
         */
        fun filterFutureBeeps(times: List<Long>, now: Long): List<Long> =
            times.filter { it > now }
    }
}
