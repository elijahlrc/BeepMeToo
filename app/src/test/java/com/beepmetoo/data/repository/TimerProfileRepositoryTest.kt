package com.beepmetoo.data.repository

import app.cash.turbine.test
import com.beepmetoo.data.db.dao.TimerProfileDao
import com.beepmetoo.data.db.entity.TimerProfile
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TimerProfileRepositoryTest {

    private lateinit var dao: TimerProfileDao
    private lateinit var repository: TimerProfileRepository

    private fun makeProfile(name: String = "Default") = TimerProfile(
        name = name,
        beepsPerDay = 5,
        startHour = 8,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
    )

    @Before
    fun setup() {
        dao = mock()
        repository = TimerProfileRepository(dao)
    }

    @Test
    fun `getActiveProfile delegates to dao`() = runTest {
        val profile = makeProfile("Active").copy(id = 1, isActive = true)
        whenever(dao.getActive()).thenReturn(flowOf(profile))

        repository.getActiveProfile().test {
            val result = awaitItem()
            assertThat(result).isNotNull()
            assertThat(result!!.name).isEqualTo("Active")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setActiveProfile delegates to dao`() = runTest {
        repository.setActiveProfile(5L)
        verify(dao).setActive(5L)
    }

    @Test
    fun `createProfile delegates to dao`() = runTest {
        val profile = makeProfile("New")
        whenever(dao.insert(profile)).thenReturn(10L)

        val id = repository.createProfile(profile)
        assertThat(id).isEqualTo(10L)
        verify(dao).insert(profile)
    }

    @Test
    fun `updateProfile delegates to dao`() = runTest {
        val profile = makeProfile("Updated").copy(id = 1)
        repository.updateProfile(profile)
        verify(dao).update(profile)
    }

    @Test
    fun `deleteProfile delegates to dao`() = runTest {
        val profile = makeProfile().copy(id = 1)
        repository.deleteProfile(profile)
        verify(dao).delete(profile)
    }
}
