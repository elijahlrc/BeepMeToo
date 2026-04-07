package com.beepmetoo.ui.settings

import com.beepmetoo.data.db.entity.TimerProfile
import com.beepmetoo.data.repository.TimerProfileRepository
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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: TimerProfileRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default profile values`() = runTest {
        whenever(repository.getActiveProfile()).thenReturn(flowOf(null))
        val viewModel = SettingsViewModel(repository)

        val state = viewModel.uiState.value
        assertThat(state.name).isEqualTo("Default")
        assertThat(state.beepsPerDay).isEqualTo(5)
        assertThat(state.startHour).isEqualTo(8)
        assertThat(state.endHour).isEqualTo(22)
    }

    @Test
    fun `loads active profile into state`() = runTest {
        val profile = TimerProfile(
            id = 1, name = "Work", isActive = true,
            beepsPerDay = 8, startHour = 9, startMinute = 30, endHour = 17, endMinute = 0,
        )
        whenever(repository.getActiveProfile()).thenReturn(flowOf(profile))

        val viewModel = SettingsViewModel(repository)

        val state = viewModel.uiState.value
        assertThat(state.editingProfileId).isEqualTo(1L)
        assertThat(state.name).isEqualTo("Work")
        assertThat(state.beepsPerDay).isEqualTo(8)
        assertThat(state.startHour).isEqualTo(9)
        assertThat(state.startMinute).isEqualTo(30)
    }

    @Test
    fun `onNameChanged updates state`() = runTest {
        whenever(repository.getActiveProfile()).thenReturn(flowOf(null))
        val viewModel = SettingsViewModel(repository)
        viewModel.onNameChanged("Evening")
        assertThat(viewModel.uiState.value.name).isEqualTo("Evening")
    }

    @Test
    fun `onBeepsPerDayChanged updates state`() = runTest {
        whenever(repository.getActiveProfile()).thenReturn(flowOf(null))
        val viewModel = SettingsViewModel(repository)
        viewModel.onBeepsPerDayChanged(10)
        assertThat(viewModel.uiState.value.beepsPerDay).isEqualTo(10)
    }

    @Test
    fun `saveProfile creates new profile when no existing`() = runTest {
        whenever(repository.getActiveProfile()).thenReturn(flowOf(null))
        whenever(repository.createProfile(argThat { name == "Default" })).thenReturn(1L)

        val viewModel = SettingsViewModel(repository)
        viewModel.saveProfile()

        verify(repository).createProfile(argThat {
            name == "Default" && beepsPerDay == 5 && startHour == 8 && endHour == 22
        })
        verify(repository).setActiveProfile(1L)
    }

    @Test
    fun `saveProfile updates existing profile`() = runTest {
        val existing = TimerProfile(
            id = 5, name = "Old", isActive = true,
            beepsPerDay = 3, startHour = 8, startMinute = 0, endHour = 22, endMinute = 0,
        )
        whenever(repository.getActiveProfile()).thenReturn(flowOf(existing))

        val viewModel = SettingsViewModel(repository)
        viewModel.onNameChanged("Updated")
        viewModel.saveProfile()

        verify(repository).updateProfile(argThat { id == 5L && name == "Updated" })
    }
}
