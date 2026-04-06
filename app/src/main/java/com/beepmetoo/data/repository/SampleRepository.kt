package com.beepmetoo.data.repository

import com.beepmetoo.data.db.dao.SampleDao
import com.beepmetoo.data.db.dao.TagDao
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleTagCrossRef
import com.beepmetoo.data.db.entity.SampleWithTags
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepository @Inject constructor(
    private val sampleDao: SampleDao,
    private val tagDao: TagDao,
) {
    suspend fun createSampleWithTags(sample: Sample, tagNames: List<String>): Long {
        val sampleId = sampleDao.insert(sample)
        for (name in tagNames) {
            val tagId = tagDao.getOrCreate(name)
            sampleDao.insertCrossRef(SampleTagCrossRef(sampleId, tagId))
        }
        return sampleId
    }

    fun getAllSamplesWithTags(): Flow<List<SampleWithTags>> =
        sampleDao.getAllWithTags()

    fun getSamplesByDateRange(start: Long, end: Long): Flow<List<Sample>> =
        sampleDao.getByDateRange(start, end)

    suspend fun updateSample(sample: Sample) =
        sampleDao.update(sample)

    suspend fun deleteSample(sample: Sample) =
        sampleDao.delete(sample)
}
