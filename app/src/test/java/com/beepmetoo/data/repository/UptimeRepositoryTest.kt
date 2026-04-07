package com.beepmetoo.data.repository

import app.cash.turbine.test
import com.beepmetoo.data.db.dao.UptimeDao
import com.beepmetoo.data.db.entity.UptimeEntry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UptimeRepositoryTest {

    private lateinit var dao: UptimeDao
    private lateinit var repository: UptimeRepository

    @Before
    fun setup() {
        dao = mock()
        repository = UptimeRepository(dao)
    }

    @Test
    fun `startSession inserts entry with null endTime`() = runTest {
        whenever(dao.insert(any())).thenReturn(1L)

        repository.startSession()

        verify(dao).insert(argThat { endTime == null && startTime > 0 })
    }

    @Test
    fun `stopSession closes the latest open entry`() = runTest {
        val openEntry = UptimeEntry(id = 5, startTime = 1000, endTime = null)
        whenever(dao.getLatestOpen()).thenReturn(openEntry)

        repository.stopSession()

        verify(dao).getLatestOpen()
        verify(dao).closeEntry(argThat { this == 5L }, argThat { this > 0 })
    }

    @Test
    fun `stopSession is no-op when no open entry exists`() = runTest {
        whenever(dao.getLatestOpen()).thenReturn(null)

        repository.stopSession()

        verify(dao).getLatestOpen()
        verify(dao, never()).closeEntry(any(), any())
    }

    @Test
    fun `getAllEntries delegates to dao`() = runTest {
        val entries = listOf(
            UptimeEntry(id = 1, startTime = 3000, endTime = 4000),
            UptimeEntry(id = 2, startTime = 1000, endTime = 2000),
        )
        whenever(dao.getAll()).thenReturn(flowOf(entries))

        repository.getAllEntries().test {
            val items = awaitItem()
            assertThat(items).hasSize(2)
            assertThat(items[0].startTime).isEqualTo(3000)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
