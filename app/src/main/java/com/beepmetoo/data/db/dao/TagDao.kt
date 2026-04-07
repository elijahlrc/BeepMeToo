package com.beepmetoo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.beepmetoo.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Tag?

    @Transaction
    suspend fun getOrCreate(name: String): Long {
        val existing = getByName(name)
        if (existing != null) return existing.id
        return insert(Tag(name = name))
    }

    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<Tag>>

    @Query(
        "SELECT t.* FROM tags t " +
        "INNER JOIN sample_tag_cross_ref st ON t.id = st.tagId " +
        "WHERE st.sampleId = :sampleId"
    )
    suspend fun getTagsForSample(sampleId: Long): List<Tag>
}
