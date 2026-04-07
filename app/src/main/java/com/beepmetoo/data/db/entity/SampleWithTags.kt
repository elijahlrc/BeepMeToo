package com.beepmetoo.data.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SampleWithTags(
    @Embedded val sample: Sample,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SampleTagCrossRef::class,
            parentColumn = "sampleId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<Tag>,
)
