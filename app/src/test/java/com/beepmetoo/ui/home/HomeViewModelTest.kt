package com.beepmetoo.ui.home

import app.cash.turbine.test
import com.beepmetoo.data.db.entity.TimerProfile
import com.beepmetoo.data.repository.TimerProfileRepository
import com.beepmetoo.data.repository.UptimeRepository
import com.beepmetoo.service.BeepScheduler
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var profileRepo: TimerProfileRepository
    private lateinit var uptimeRepo: UptimeRepository
    private lateinit var scheduler: BeepScheduler
    private val testDispatcher = UnconfinedTestDispatcher()

    private val activeProfile = TimerProfile(
        id = 1, name = "Default", isActive = true,
        beepsPerDay = 5, startHour = 8, startMinute = 0, endHour = 22, endMinute = 0,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        profileRepo = mock()
        uptimeRepo = mock()
        scheduler = mock()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState shows active profile when one exists`() = runTest {
        whenever(profileRepo.getActiveProfile()).thenReturn(flowOf(activeProfile))

        val viewModel = HomeViewModel(profileRepo, uptimeRepo, scheduler)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.activeProfile).isNotNull()
            assertThat(state.activeProfile!!.name).isEqualTo("Default")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState shows null profile when none active`() = runTest {
        whenever(profileRepo.getActiveProfile()).thenReturn(flowOf(null))

        val viewModel = HomeViewModel(profileRepo, uptimeRepo, scheduler)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.activeProfile).isNull()
            assertThat(state.isBeeping).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startBeeping schedules beeps and records uptime`() = runTest {
        whenever(profileRepo.getActiveProfile()).thenReturn(flowOf(activeProfile))

        val viewModel = HomeViewModel(profileRepo, uptimeRepo, scheduler)
        viewModel.startBeeping()

        verify(scheduler).scheduleBeepsForDate(activeProfile)
        verify(uptimeRepo).startSession()
        assertThat(viewModel.uiState.value.isBeeping).isTrue()
    }

    @Test
    fun `stopBeeping cancels beeps and stops uptime`() = runTest {
        whenever(profileRepo.getActiveProfile()).thenReturn(flowOf(activeProfile))

        val viewModel = HomeViewModel(profileRepo, uptimeRepo, scheduler)
        viewModel.startBeeping()
        viewModel.stopBeeping()

        verify(scheduler).cancelBeepsForProfile(activeProfile.id)
        verify(uptimeRepo).stopSession()
        assertThat(viewModel.uiState.value.isBeeping).isFalse()
    }
}
