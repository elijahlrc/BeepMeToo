package com.beepmetoo.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.beepmetoo.data.db.BeepMeTooDatabase
import com.beepmetoo.data.db.entity.TimerProfile
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerProfileDaoTest {

    private lateinit var database: BeepMeTooDatabase
    private lateinit var dao: TimerProfileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeepMeTooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.timerProfileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun makeProfile(
        name: String = "Default",
        isActive: Boolean = false,
    ) = TimerProfile(
        name = name,
        isActive = isActive,
        beepsPerDay = 5,
        startHour = 8,
        startMinute = 0,
        endHour = 22,
        endMinute = 0,
    )

    @Test
    fun insert_returnsPositiveId() = runTest {
        val id = dao.insert(makeProfile())
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getById_returnsInsertedProfile() = runTest {
        val id = dao.insert(makeProfile(name = "Morning"))
        val profile = dao.getById(id)
        assertThat(profile).isNotNull()
        assertThat(profile!!.name).isEqualTo("Morning")
        assertThat(profile.beepsPerDay).isEqualTo(5)
    }

    @Test
    fun getById_returnsNullForMissing() = runTest {
        assertThat(dao.getById(999)).isNull()
    }

    @Test
    fun getActive_returnsActiveProfile() = runTest {
        dao.insert(makeProfile(name = "Inactive", isActive = false))
        dao.insert(makeProfile(name = "Active", isActive = true))

        dao.getActive().test {
            val active = awaitItem()
            assertThat(active).isNotNull()
            assertThat(active!!.name).isEqualTo("Active")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getActive_returnsNullWhenNoneActive() = runTest {
        dao.insert(makeProfile(isActive = false))

        dao.getActive().test {
            val active = awaitItem()
            assertThat(active).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setActive_deactivatesOthersAndActivatesTarget() = runTest {
        val id1 = dao.insert(makeProfile(name = "First", isActive = true))
        val id2 = dao.insert(makeProfile(name = "Second", isActive = false))

        dao.setActive(id2)

        val first = dao.getById(id1)!!
        val second = dao.getById(id2)!!
        assertThat(first.isActive).isFalse()
        assertThat(second.isActive).isTrue()
    }

    @Test
    fun update_modifiesFields() = runTest {
        val id = dao.insert(makeProfile(name = "Original"))
        val profile = dao.getById(id)!!
        dao.update(profile.copy(name = "Updated", beepsPerDay = 10))

        val updated = dao.getById(id)!!
        assertThat(updated.name).isEqualTo("Updated")
        assertThat(updated.beepsPerDay).isEqualTo(10)
    }

    @Test
    fun delete_removesProfile() = runTest {
        val id = dao.insert(makeProfile())
        val profile = dao.getById(id)!!
        dao.delete(profile)
        assertThat(dao.getById(id)).isNull()
    }
}
