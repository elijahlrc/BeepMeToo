package com.beepmetoo.data.repository

import app.cash.turbine.test
import com.beepmetoo.data.db.dao.SampleDao
import com.beepmetoo.data.db.dao.TagDao
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleTagCrossRef
import com.beepmetoo.data.db.entity.SampleWithTags
import com.beepmetoo.data.db.entity.Tag
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SampleRepositoryTest {

    private lateinit var sampleDao: SampleDao
    private lateinit var tagDao: TagDao
    private lateinit var repository: SampleRepository

    @Before
    fun setup() {
        sampleDao = mock()
        tagDao = mock()
        repository = SampleRepository(sampleDao, tagDao)
    }

    @Test
    fun `createSampleWithTags inserts sample and cross-refs for each tag`() = runTest {
        val sample = Sample(title = "Test", timestamp = 1000, createdAt = 1001)
        whenever(sampleDao.insert(sample)).thenReturn(42L)
        whenever(tagDao.getOrCreate("mood")).thenReturn(1L)
        whenever(tagDao.getOrCreate("work")).thenReturn(2L)

        val id = repository.createSampleWithTags(sample, listOf("mood", "work"))

        assertThat(id).isEqualTo(42L)
        verify(sampleDao).insert(sample)
        verify(tagDao).getOrCreate("mood")
        verify(tagDao).getOrCreate("work")
        verify(sampleDao).insertCrossRef(SampleTagCrossRef(42L, 1L))
        verify(sampleDao).insertCrossRef(SampleTagCrossRef(42L, 2L))
    }

    @Test
    fun `createSampleWithTags with empty tags inserts sample only`() = runTest {
        val sample = Sample(title = "Test", timestamp = 1000, createdAt = 1001)
        whenever(sampleDao.insert(sample)).thenReturn(42L)

        val id = repository.createSampleWithTags(sample, emptyList())

        assertThat(id).isEqualTo(42L)
        verify(sampleDao).insert(sample)
        verify(tagDao, never()).getOrCreate(any())
        verify(sampleDao, never()).insertCrossRef(any())
    }

    @Test
    fun `getAllSamplesWithTags delegates to sampleDao`() = runTest {
        val expected = listOf(
            SampleWithTags(
                sample = Sample(id = 1, title = "Test", timestamp = 1000, createdAt = 1001),
                tags = listOf(Tag(id = 1, name = "mood")),
            )
        )
        whenever(sampleDao.getAllWithTags()).thenReturn(flowOf(expected))

        repository.getAllSamplesWithTags().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items[0].sample.title).isEqualTo("Test")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSample delegates to sampleDao`() = runTest {
        val sample = Sample(id = 1, title = "Updated", timestamp = 1000, createdAt = 1001)
        repository.updateSample(sample)
        verify(sampleDao).update(sample)
    }

    @Test
    fun `deleteSample delegates to sampleDao`() = runTest {
        val sample = Sample(id = 1, title = "Test", timestamp = 1000, createdAt = 1001)
        repository.deleteSample(sample)
        verify(sampleDao).delete(sample)
    }
}
