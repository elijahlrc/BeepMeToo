package com.beepmetoo.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.db.dao.SampleDao
import com.beepmetoo.data.db.dao.TagDao
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.Tag
import com.beepmetoo.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val sample: Sample? = null,
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val sampleDao: SampleDao,
    private val tagDao: TagDao,
    private val repository: SampleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sampleId: Long = savedStateHandle["sampleId"] ?: -1L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadSample()
    }

    private fun loadSample() {
        viewModelScope.launch {
            val sample = sampleDao.getById(sampleId)
            val tags = if (sample != null) tagDao.getTagsForSample(sampleId) else emptyList()
            _uiState.update { it.copy(sample = sample, tags = tags, isLoading = false) }
        }
    }

    fun deleteSample() {
        viewModelScope.launch {
            val sample = _uiState.value.sample ?: return@launch
            repository.deleteSample(sample)
        }
    }
}
