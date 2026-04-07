package com.beepmetoo.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "sample_tag_cross_ref",
    primaryKeys = ["sampleId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Sample::class,
            parentColumns = ["id"],
            childColumns = ["sampleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SampleTagCrossRef(
    val sampleId: Long,
    val tagId: Long,
)
