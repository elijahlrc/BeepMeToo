package com.beepmetoo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.db.entity.TimerProfile
import com.beepmetoo.data.repository.TimerProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val editingProfileId: Long? = null,
    val name: String = "Default",
    val beepsPerDay: Int = 5,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val isSaved: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TimerProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val active = repository.getActiveProfile().first()
            if (active != null) {
                _uiState.update {
                    it.copy(
                        editingProfileId = active.id,
                        name = active.name,
                        beepsPerDay = active.beepsPerDay,
                        startHour = active.startHour,
                        startMinute = active.startMinute,
                        endHour = active.endHour,
                        endMinute = active.endMinute,
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onBeepsPerDayChanged(count: Int) {
        _uiState.update { it.copy(beepsPerDay = count.coerceIn(1, 50)) }
    }

    fun onStartHourChanged(hour: Int) {
        _uiState.update { it.copy(startHour = hour.coerceIn(0, 23)) }
    }

    fun onStartMinuteChanged(minute: Int) {
        _uiState.update { it.copy(startMinute = minute.coerceIn(0, 59)) }
    }

    fun onEndHourChanged(hour: Int) {
        _uiState.update { it.copy(endHour = hour.coerceIn(0, 23)) }
    }

    fun onEndMinuteChanged(minute: Int) {
        _uiState.update { it.copy(endMinute = minute.coerceIn(0, 59)) }
    }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.editingProfileId != null) {
                repository.updateProfile(
                    TimerProfile(
                        id = state.editingProfileId,
                        name = state.name,
                        isActive = true,
                        beepsPerDay = state.beepsPerDay,
                        startHour = state.startHour,
                        startMinute = state.startMinute,
                        endHour = state.endHour,
                        endMinute = state.endMinute,
                    )
                )
            } else {
                val id = repository.createProfile(
                    TimerProfile(
                        name = state.name,
                        beepsPerDay = state.beepsPerDay,
                        startHour = state.startHour,
                        startMinute = state.startMinute,
                        endHour = state.endHour,
                        endMinute = state.endMinute,
                    )
                )
                repository.setActiveProfile(id)
                _uiState.update { it.copy(editingProfileId = id) }
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
