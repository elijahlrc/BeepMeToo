package com.beepmetoo.service

import com.beepmetoo.data.db.entity.TimerProfile
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class BeepSchedulerTest {

    private fun makeProfile(
        beepsPerDay: Int = 5,
        startHour: Int = 8,
        startMinute: Int = 0,
        endHour: Int = 22,
        endMinute: Int = 0,
    ) = TimerProfile(
        id = 1,
        name = "Test",
        isActive = true,
        beepsPerDay = beepsPerDay,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
    )

    private val zone = ZoneId.systemDefault()

    @Test
    fun `computeBeepTimes returns correct number of beeps`() {
        val profile = makeProfile(beepsPerDay = 5)
        val date = LocalDate.of(2026, 4, 6)

        val times = BeepScheduler.computeBeepTimes(profile, date, zone)

        assertThat(times).hasSize(5)
    }

    @Test
    fun `computeBeepTimes returns times within active window`() {
        val profile = makeProfile(startHour = 9, startMinute = 30, endHour = 17, endMinute = 0)
        val date = LocalDate.of(2026, 4, 6)

        val windowStart = date.atTime(LocalTime.of(9, 30))
            .atZone(zone).toInstant().toEpochMilli()
        val windowEnd = date.atTime(LocalTime.of(17, 0))
            .atZone(zone).toInstant().toEpochMilli()

        val times = BeepScheduler.computeBeepTimes(profile, date, zone)

        for (time in times) {
            assertThat(time).isAtLeast(windowStart)
            assertThat(time).isLessThan(windowEnd)
        }
    }

    @Test
    fun `computeBeepTimes returns sorted times`() {
        val profile = makeProfile(beepsPerDay = 10)
        val date = LocalDate.of(2026, 4, 6)

        val times = BeepScheduler.computeBeepTimes(profile, date, zone)

        assertThat(times).isInStrictOrder()
    }

    @Test
    fun `computeBeepTimes with 1 beep returns single time in window`() {
        val profile = makeProfile(beepsPerDay = 1, startHour = 8, endHour = 22)
        val date = LocalDate.of(2026, 4, 6)

        val times = BeepScheduler.computeBeepTimes(profile, date, zone)

        assertThat(times).hasSize(1)
    }

    @Test
    fun `computeBeepTimes distributes across slots`() {
        // With 4 beeps in a 4-hour window (12:00-16:00), each slot is 1 hour.
        // Each beep should land in its own 1-hour slot.
        val profile = makeProfile(beepsPerDay = 4, startHour = 12, startMinute = 0, endHour = 16, endMinute = 0)
        val date = LocalDate.of(2026, 4, 6)

        val times = BeepScheduler.computeBeepTimes(profile, date, zone)

        val slotDurationMs = (4 * 60 * 60 * 1000L) / 4 // 1 hour per slot
        val windowStart = date.atTime(LocalTime.of(12, 0))
            .atZone(zone).toInstant().toEpochMilli()

        for (i in times.indices) {
            val slotStart = windowStart + (i * slotDurationMs)
            val slotEnd = slotStart + slotDurationMs
            assertThat(times[i]).isAtLeast(slotStart)
            assertThat(times[i]).isLessThan(slotEnd)
        }
    }

    @Test
    fun `computeBeepTimes produces different results on repeated calls`() {
        // Statistical test: run 10 times, at least 2 should differ
        val profile = makeProfile(beepsPerDay = 5)
        val date = LocalDate.of(2026, 4, 6)

        val results = (1..10).map { BeepScheduler.computeBeepTimes(profile, date, zone) }
        val distinct = results.distinct().size

        assertThat(distinct).isGreaterThan(1)
    }

    @Test
    fun `filterFutureBeeps removes past times`() {
        val now = 5000L
        val times = listOf(1000L, 3000L, 5000L, 7000L, 9000L)

        val future = BeepScheduler.filterFutureBeeps(times, now)

        assertThat(future).containsExactly(7000L, 9000L)
    }

    @Test
    fun `filterFutureBeeps with all past returns empty`() {
        val future = BeepScheduler.filterFutureBeeps(listOf(100L, 200L), 500L)
        assertThat(future).isEmpty()
    }

    @Test
    fun `filterFutureBeeps with all future returns all`() {
        val future = BeepScheduler.filterFutureBeeps(listOf(600L, 700L), 500L)
        assertThat(future).containsExactly(600L, 700L)
    }
}
