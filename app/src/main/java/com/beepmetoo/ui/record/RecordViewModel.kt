package com.beepmetoo.ui.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordUiState(
    val title: String = "",
    val description: String = "",
    val tagsInput: String = "",
    val photoPath: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: SampleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val beepTimestamp: Long = savedStateHandle["beepTimestamp"] ?: -1L

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTagsInputChanged(tagsInput: String) {
        _uiState.update { it.copy(tagsInput = tagsInput) }
    }

    fun onPhotoPathChanged(photoPath: String?) {
        _uiState.update { it.copy(photoPath = photoPath) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val timestamp = if (beepTimestamp > 0) beepTimestamp else System.currentTimeMillis()
            val sample = Sample(
                title = state.title.trim(),
                description = state.description.trim().ifEmpty { null },
                photoPath = state.photoPath,
                timestamp = timestamp,
                createdAt = System.currentTimeMillis(),
            )
            val tagNames = state.tagsInput
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            repository.createSampleWithTags(sample, tagNames)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
