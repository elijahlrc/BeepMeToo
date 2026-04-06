package com.beepmetoo.ui.record

import androidx.lifecycle.SavedStateHandle
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.repository.SampleRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private lateinit var repository: SampleRepository
    private lateinit var viewModel: RecordViewModel
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

    private fun createViewModel(beepTimestamp: Long = -1L): RecordViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("beepTimestamp" to beepTimestamp))
        return RecordViewModel(repository, savedStateHandle)
    }

    @Test
    fun `initial state has empty fields`() {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertThat(state.title).isEmpty()
        assertThat(state.description).isEmpty()
        assertThat(state.tagsInput).isEmpty()
        assertThat(state.photoPath).isNull()
        assertThat(state.isSaved).isFalse()
    }

    @Test
    fun `beepTimestamp from savedStateHandle is captured`() {
        viewModel = createViewModel(beepTimestamp = 12345L)
        assertThat(viewModel.beepTimestamp).isEqualTo(12345L)
    }

    @Test
    fun `onTitleChanged updates state`() {
        viewModel = createViewModel()
        viewModel.onTitleChanged("My experience")
        assertThat(viewModel.uiState.value.title).isEqualTo("My experience")
    }

    @Test
    fun `onDescriptionChanged updates state`() {
        viewModel = createViewModel()
        viewModel.onDescriptionChanged("Felt calm")
        assertThat(viewModel.uiState.value.description).isEqualTo("Felt calm")
    }

    @Test
    fun `onTagsInputChanged updates state`() {
        viewModel = createViewModel()
        viewModel.onTagsInputChanged("mood, work")
        assertThat(viewModel.uiState.value.tagsInput).isEqualTo("mood, work")
    }

    @Test
    fun `onPhotoPathChanged updates state`() {
        viewModel = createViewModel()
        viewModel.onPhotoPathChanged("/path/to/photo.jpg")
        assertThat(viewModel.uiState.value.photoPath).isEqualTo("/path/to/photo.jpg")
    }

    @Test
    fun `save creates sample with tags and sets isSaved`() = runTest {
        whenever(repository.createSampleWithTags(
            argThat<Sample> { title == "Test" },
            eq(listOf("mood", "work")),
        )).thenReturn(1L)

        viewModel = createViewModel(beepTimestamp = 5000L)
        viewModel.onTitleChanged("Test")
        viewModel.onDescriptionChanged("Desc")
        viewModel.onTagsInputChanged("mood, work")

        viewModel.save()

        verify(repository).createSampleWithTags(
            argThat { title == "Test" && description == "Desc" && timestamp == 5000L },
            eq(listOf("mood", "work")),
        )
        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun `save with no beepTimestamp uses current time`() = runTest {
        whenever(repository.createSampleWithTags(
            argThat<Sample> { timestamp > 0 },
            eq(emptyList()),
        )).thenReturn(1L)

        viewModel = createViewModel(beepTimestamp = -1L)
        viewModel.onTitleChanged("Manual")
        viewModel.save()

        verify(repository).createSampleWithTags(
            argThat { title == "Manual" && timestamp > 0 },
            eq(emptyList()),
        )
    }

    @Test
    fun `save trims and splits tags correctly`() = runTest {
        whenever(repository.createSampleWithTags(
            argThat<Sample> { true },
            eq(listOf("a", "b", "c")),
        )).thenReturn(1L)

        viewModel = createViewModel()
        viewModel.onTitleChanged("T")
        viewModel.onTagsInputChanged(" a , b , c ")
        viewModel.save()

        verify(repository).createSampleWithTags(
            argThat { true },
            eq(listOf("a", "b", "c")),
        )
    }

    @Test
    fun `save with empty tags input passes empty list`() = runTest {
        whenever(repository.createSampleWithTags(
            argThat<Sample> { true },
            eq(emptyList()),
        )).thenReturn(1L)

        viewModel = createViewModel()
        viewModel.onTitleChanged("T")
        viewModel.onTagsInputChanged("  ")
        viewModel.save()

        verify(repository).createSampleWithTags(
            argThat { true },
            eq(emptyList()),
        )
    }

    @Test
    fun `save with empty title does not save`() = runTest {
        viewModel = createViewModel()
        viewModel.onTitleChanged("")
        viewModel.save()

        verify(repository, never()).createSampleWithTags(
            argThat<Sample> { true },
            argThat<List<String>> { true },
        )
        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }
}
