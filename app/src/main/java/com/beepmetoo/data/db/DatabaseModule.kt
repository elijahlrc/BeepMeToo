package com.beepmetoo.data.db

import android.content.Context
import androidx.room.Room
import com.beepmetoo.data.db.dao.SampleDao
import com.beepmetoo.data.db.dao.ScheduledBeepDao
import com.beepmetoo.data.db.dao.TagDao
import com.beepmetoo.data.db.dao.TimerProfileDao
import com.beepmetoo.data.db.dao.UptimeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BeepMeTooDatabase =
        Room.databaseBuilder(context, BeepMeTooDatabase::class.java, "beepmetoo.db")
            .build()

    @Provides
    fun provideSampleDao(db: BeepMeTooDatabase): SampleDao = db.sampleDao()

    @Provides
    fun provideTagDao(db: BeepMeTooDatabase): TagDao = db.tagDao()

    @Provides
    fun provideTimerProfileDao(db: BeepMeTooDatabase): TimerProfileDao = db.timerProfileDao()

    @Provides
    fun provideScheduledBeepDao(db: BeepMeTooDatabase): ScheduledBeepDao = db.scheduledBeepDao()

    @Provides
    fun provideUptimeDao(db: BeepMeTooDatabase): UptimeDao = db.uptimeDao()
}
