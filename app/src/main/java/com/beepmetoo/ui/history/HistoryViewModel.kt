package com.beepmetoo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleWithTags
import com.beepmetoo.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SampleRepository,
) : ViewModel() {

    val samples: StateFlow<List<SampleWithTags>> =
        repository.getAllSamplesWithTags()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSample(sample: Sample) {
        viewModelScope.launch {
            repository.deleteSample(sample)
        }
    }
}
