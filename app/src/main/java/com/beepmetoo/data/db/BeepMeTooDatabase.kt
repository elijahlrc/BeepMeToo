package com.beepmetoo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beepmetoo.data.db.dao.SampleDao
import com.beepmetoo.data.db.dao.ScheduledBeepDao
import com.beepmetoo.data.db.dao.TagDao
import com.beepmetoo.data.db.dao.TimerProfileDao
import com.beepmetoo.data.db.dao.UptimeDao
import com.beepmetoo.data.db.entity.Sample
import com.beepmetoo.data.db.entity.SampleTagCrossRef
import com.beepmetoo.data.db.entity.ScheduledBeep
import com.beepmetoo.data.db.entity.Tag
import com.beepmetoo.data.db.entity.TimerProfile
import com.beepmetoo.data.db.entity.UptimeEntry

@Database(
    entities = [
        Sample::class,
        Tag::class,
        SampleTagCrossRef::class,
        TimerProfile::class,
        ScheduledBeep::class,
        UptimeEntry::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class BeepMeTooDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun tagDao(): TagDao
    abstract fun timerProfileDao(): TimerProfileDao
    abstract fun scheduledBeepDao(): ScheduledBeepDao
    abstract fun uptimeDao(): UptimeDao
}
