package com.beepmetoo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.db.entity.TimerProfile
import com.beepmetoo.data.repository.TimerProfileRepository
import com.beepmetoo.data.repository.UptimeRepository
import com.beepmetoo.service.BeepScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeProfile: TimerProfile? = null,
    val isBeeping: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepo: TimerProfileRepository,
    private val uptimeRepo: UptimeRepository,
    private val scheduler: BeepScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepo.getActiveProfile().collect { profile ->
                _uiState.update { it.copy(activeProfile = profile) }
            }
        }
    }

    fun startBeeping() {
        val profile = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            scheduler.scheduleBeepsForDate(profile)
            uptimeRepo.startSession()
            _uiState.update { it.copy(isBeeping = true) }
        }
    }

    fun stopBeeping() {
        val profile = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            scheduler.cancelBeepsForProfile(profile.id)
            uptimeRepo.stopSession()
            _uiState.update { it.copy(isBeeping = false) }
        }
    }
}
