package com.beepmetoo.ui.history

import app.cash.turbine.test
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleWithTags
import com.beepmetoo.data.db.entity.Tag
import com.beepmetoo.data.repository.SampleRepository
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
class HistoryViewModelTest {

    private lateinit var repository: SampleRepository
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
    fun `samples flow emits repository data`() = runTest {
        val data = listOf(
            SampleWithTags(
                sample = Sample(id = 1, title = "First", timestamp = 2000, createdAt = 2001),
                tags = listOf(Tag(id = 1, name = "mood")),
            ),
            SampleWithTags(
                sample = Sample(id = 2, title = "Second", timestamp = 1000, createdAt = 1001),
                tags = emptyList(),
            ),
        )
        whenever(repository.getAllSamplesWithTags()).thenReturn(flowOf(data))

        val viewModel = HistoryViewModel(repository)

        viewModel.samples.test {
            val items = awaitItem()
            assertThat(items).hasSize(2)
            assertThat(items[0].sample.title).isEqualTo("First")
            assertThat(items[0].tags).hasSize(1)
            assertThat(items[1].sample.title).isEqualTo("Second")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `samples flow emits empty list when no samples`() = runTest {
        whenever(repository.getAllSamplesWithTags()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(repository)

        viewModel.samples.test {
            val items = awaitItem()
            assertThat(items).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteSample delegates to repository`() = runTest {
        whenever(repository.getAllSamplesWithTags()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(repository)
        val sample = Sample(id = 1, title = "Test", timestamp = 1000, createdAt = 1001)

        viewModel.deleteSample(sample)

        verify(repository).deleteSample(sample)
    }
}
