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
class TagDaoTest {

    private lateinit var database: BeepMeTooDatabase
    private lateinit var tagDao: TagDao
    private lateinit var sampleDao: SampleDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeepMeTooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tagDao = database.tagDao()
        sampleDao = database.sampleDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_returnsPositiveId() = runTest {
        val id = tagDao.insert(Tag(name = "mood"))
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun insert_duplicateNameReturnsNegative() = runTest {
        tagDao.insert(Tag(name = "mood"))
        val duplicateId = tagDao.insert(Tag(name = "mood"))
        assertThat(duplicateId).isEqualTo(-1)
    }

    @Test
    fun getByName_findsExistingTag() = runTest {
        tagDao.insert(Tag(name = "work"))
        val tag = tagDao.getByName("work")
        assertThat(tag).isNotNull()
        assertThat(tag!!.name).isEqualTo("work")
    }

    @Test
    fun getByName_returnsNullForMissing() = runTest {
        val tag = tagDao.getByName("nonexistent")
        assertThat(tag).isNull()
    }

    @Test
    fun getOrCreate_insertsNewTag() = runTest {
        val id = tagDao.getOrCreate("new_tag")
        assertThat(id).isGreaterThan(0)
        val tag = tagDao.getByName("new_tag")
        assertThat(tag).isNotNull()
    }

    @Test
    fun getOrCreate_returnsExistingTagId() = runTest {
        val firstId = tagDao.getOrCreate("existing")
        val secondId = tagDao.getOrCreate("existing")
        assertThat(firstId).isEqualTo(secondId)
    }

    @Test
    fun getAll_emitsAllTags() = runTest {
        tagDao.insert(Tag(name = "a"))
        tagDao.insert(Tag(name = "b"))
        tagDao.insert(Tag(name = "c"))

        tagDao.getAll().test {
            val tags = awaitItem()
            assertThat(tags).hasSize(3)
            assertThat(tags.map { it.name }).containsExactly("a", "b", "c")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTagsForSample_returnsLinkedTags() = runTest {
        val sampleId = sampleDao.insert(
            Sample(title = "Test", timestamp = 1000, createdAt = 1001)
        )
        val tagId1 = tagDao.insert(Tag(name = "mood"))
        val tagId2 = tagDao.insert(Tag(name = "work"))
        val unlinkedTagId = tagDao.insert(Tag(name = "unlinked"))

        sampleDao.insertCrossRef(SampleTagCrossRef(sampleId, tagId1))
        sampleDao.insertCrossRef(SampleTagCrossRef(sampleId, tagId2))

        val tags = tagDao.getTagsForSample(sampleId)
        assertThat(tags).hasSize(2)
        assertThat(tags.map { it.name }).containsExactly("mood", "work")
    }
}
