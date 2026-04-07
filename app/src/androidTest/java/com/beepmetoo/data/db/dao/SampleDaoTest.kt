package com.beepmetoo.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.beepmetoo.data.db.BeepMeTooDatabase
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleTagCrossRef
import com.beepmetoo.data.db.entity.Tag
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleDaoTest {

    private lateinit var database: BeepMeTooDatabase
    private lateinit var sampleDao: SampleDao
    private lateinit var tagDao: TagDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeepMeTooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sampleDao = database.sampleDao()
        tagDao = database.tagDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun makeSample(
        title: String = "Test",
        timestamp: Long = 1000L,
        createdAt: Long = 1001L,
    ) = Sample(title = title, timestamp = timestamp, createdAt = createdAt)

    @Test
    fun insert_returnsPositiveId() = runTest {
        val id = sampleDao.insert(makeSample())
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getById_returnsInsertedSample() = runTest {
        val id = sampleDao.insert(makeSample(title = "Hello"))
        val sample = sampleDao.getById(id)
        assertThat(sample).isNotNull()
        assertThat(sample!!.title).isEqualTo("Hello")
        assertThat(sample.id).isEqualTo(id)
    }

    @Test
    fun getById_returnsNullForMissingId() = runTest {
        val sample = sampleDao.getById(999)
        assertThat(sample).isNull()
    }

    @Test
    fun getAll_emitsSamplesOrderedByTimestampDesc() = runTest {
        sampleDao.insert(makeSample(title = "Old", timestamp = 100))
        sampleDao.insert(makeSample(title = "New", timestamp = 300))
        sampleDao.insert(makeSample(title = "Mid", timestamp = 200))

        sampleDao.getAll().test {
            val samples = awaitItem()
            assertThat(samples).hasSize(3)
            assertThat(samples[0].title).isEqualTo("New")
            assertThat(samples[1].title).isEqualTo("Mid")
            assertThat(samples[2].title).isEqualTo("Old")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun update_modifiesTitleAndDescription() = runTest {
        val id = sampleDao.insert(makeSample(title = "Original"))
        val original = sampleDao.getById(id)!!
        sampleDao.update(original.copy(title = "Updated", description = "New desc"))

        val updated = sampleDao.getById(id)!!
        assertThat(updated.title).isEqualTo("Updated")
        assertThat(updated.description).isEqualTo("New desc")
    }

    @Test
    fun delete_removesTheSample() = runTest {
        val id = sampleDao.insert(makeSample())
        val sample = sampleDao.getById(id)!!
        sampleDao.delete(sample)
        assertThat(sampleDao.getById(id)).isNull()
    }

    @Test
    fun getByDateRange_returnsOnlyMatchingSamples() = runTest {
        sampleDao.insert(makeSample(title = "Before", timestamp = 50))
        sampleDao.insert(makeSample(title = "In range", timestamp = 150))
        sampleDao.insert(makeSample(title = "After", timestamp = 350))

        sampleDao.getByDateRange(100, 300).test {
            val samples = awaitItem()
            assertThat(samples).hasSize(1)
            assertThat(samples[0].title).isEqualTo("In range")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllWithTags_returnsSamplesWithCorrectTags() = runTest {
        val sampleId = sampleDao.insert(makeSample(title = "Tagged"))
        val tagId1 = tagDao.insert(Tag(name = "mood"))
        val tagId2 = tagDao.insert(Tag(name = "work"))
        sampleDao.insertCrossRef(SampleTagCrossRef(sampleId, tagId1))
        sampleDao.insertCrossRef(SampleTagCrossRef(sampleId, tagId2))

        sampleDao.getAllWithTags().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items[0].sample.title).isEqualTo("Tagged")
            assertThat(items[0].tags.map { it.name }).containsExactly("mood", "work")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllWithTags_sampleWithNoTagsHasEmptyList() = runTest {
        sampleDao.insert(makeSample(title = "No tags"))

        sampleDao.getAllWithTags().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items[0].tags).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
