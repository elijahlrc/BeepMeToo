package com.beepmetoo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleTagCrossRef
import com.beepmetoo.data.db.entity.SampleWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {

    @Insert
    suspend fun insert(sample: Sample): Long

    @Update
    suspend fun update(sample: Sample)

    @Delete
    suspend fun delete(sample: Sample)

    @Query("SELECT * FROM samples WHERE id = :id")
    suspend fun getById(id: Long): Sample?

    @Query("SELECT * FROM samples ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Sample>>

    @Query("SELECT * FROM samples WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<Sample>>

    @Transaction
    @Query("SELECT * FROM samples ORDER BY timestamp DESC")
    fun getAllWithTags(): Flow<List<SampleWithTags>>

    @Insert
    suspend fun insertCrossRef(crossRef: SampleTagCrossRef)
}
