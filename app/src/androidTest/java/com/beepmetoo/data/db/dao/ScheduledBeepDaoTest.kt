package com.beepmetoo.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.beepmetoo.data.db.BeepMeTooDatabase
import com.beepmetoo.data.db.entity.ScheduledBeep
import com.beepmetoo.data.db.entity.TimerProfile
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduledBeepDaoTest {

    private lateinit var database: BeepMeTooDatabase
    private lateinit var beepDao: ScheduledBeepDao
    private lateinit var profileDao: TimerProfileDao
    private var profileId: Long = 0

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeepMeTooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        beepDao = database.scheduledBeepDao()
        profileDao = database.timerProfileDao()

        // Insert a timer profile since ScheduledBeep has FK to TimerProfile
        profileId = profileDao.insert(
            TimerProfile(
                name = "Test",
                beepsPerDay = 5,
                startHour = 8,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
            )
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_returnsPositiveId() = runTest {
        val id = beepDao.insert(ScheduledBeep(scheduledTime = 1000, timerProfileId = profileId))
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getUnfired_returnsOnlyUnfiredBeepsOrderedByTime() = runTest {
        beepDao.insert(ScheduledBeep(scheduledTime = 300, timerProfileId = profileId, fired = false))
        beepDao.insert(ScheduledBeep(scheduledTime = 100, timerProfileId = profileId, fired = false))
        beepDao.insert(ScheduledBeep(scheduledTime = 200, timerProfileId = profileId, fired = true))

        beepDao.getUnfired().test {
            val beeps = awaitItem()
            assertThat(beeps).hasSize(2)
            assertThat(beeps[0].scheduledTime).isEqualTo(100)
            assertThat(beeps[1].scheduledTime).isEqualTo(300)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markFired_setsFiredTrue() = runTest {
        val id = beepDao.insert(ScheduledBeep(scheduledTime = 1000, timerProfileId = profileId))

        beepDao.markFired(id)

        beepDao.getUnfired().test {
            val beeps = awaitItem()
            assertThat(beeps).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByProfile_removesAllBeepsForProfile() = runTest {
        val profileId2 = profileDao.insert(
            TimerProfile(
                name = "Other",
                beepsPerDay = 3,
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
            )
        )

        beepDao.insert(ScheduledBeep(scheduledTime = 100, timerProfileId = profileId))
        beepDao.insert(ScheduledBeep(scheduledTime = 200, timerProfileId = profileId))
        beepDao.insert(ScheduledBeep(scheduledTime = 300, timerProfileId = profileId2))

        beepDao.deleteByProfile(profileId)

        beepDao.getUnfired().test {
            val beeps = awaitItem()
            assertThat(beeps).hasSize(1)
            assertThat(beeps[0].timerProfileId).isEqualTo(profileId2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
