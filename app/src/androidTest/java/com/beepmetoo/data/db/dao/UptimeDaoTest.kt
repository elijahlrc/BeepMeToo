package com.beepmetoo.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.beepmetoo.data.db.BeepMeTooDatabase
import com.beepmetoo.data.db.entity.UptimeEntry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UptimeDaoTest {

    private lateinit var database: BeepMeTooDatabase
    private lateinit var dao: UptimeDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeepMeTooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.uptimeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_returnsPositiveId() = runTest {
        val id = dao.insert(UptimeEntry(startTime = 1000))
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getLatestOpen_returnsEntryWithNullEndTime() = runTest {
        dao.insert(UptimeEntry(startTime = 1000, endTime = 2000)) // closed
        dao.insert(UptimeEntry(startTime = 3000, endTime = null)) // open

        val open = dao.getLatestOpen()
        assertThat(open).isNotNull()
        assertThat(open!!.startTime).isEqualTo(3000)
        assertThat(open.endTime).isNull()
    }

    @Test
    fun getLatestOpen_returnsHighestStartTimeWhenMultipleOpen() = runTest {
        dao.insert(UptimeEntry(startTime = 1000))
        dao.insert(UptimeEntry(startTime = 5000))
        dao.insert(UptimeEntry(startTime = 3000))

        val open = dao.getLatestOpen()
        assertThat(open).isNotNull()
        assertThat(open!!.startTime).isEqualTo(5000)
    }

    @Test
    fun getLatestOpen_returnsNullWhenAllClosed() = runTest {
        dao.insert(UptimeEntry(startTime = 1000, endTime = 2000))
        dao.insert(UptimeEntry(startTime = 3000, endTime = 4000))

        val open = dao.getLatestOpen()
        assertThat(open).isNull()
    }

    @Test
    fun closeEntry_setsEndTime() = runTest {
        val id = dao.insert(UptimeEntry(startTime = 1000))
        dao.closeEntry(id, 5000)

        val entry = dao.getLatestOpen()
        assertThat(entry).isNull()
    }

    @Test
    fun getAll_returnsEntriesOrderedByStartTimeDesc() = runTest {
        dao.insert(UptimeEntry(startTime = 100, endTime = 200))
        dao.insert(UptimeEntry(startTime = 500, endTime = 600))
        dao.insert(UptimeEntry(startTime = 300, endTime = 400))

        dao.getAll().test {
            val entries = awaitItem()
            assertThat(entries).hasSize(3)
            assertThat(entries[0].startTime).isEqualTo(500)
            assertThat(entries[1].startTime).isEqualTo(300)
            assertThat(entries[2].startTime).isEqualTo(100)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
