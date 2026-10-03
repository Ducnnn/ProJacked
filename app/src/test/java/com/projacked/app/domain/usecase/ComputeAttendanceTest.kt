package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ComputeAttendanceTest {

    private val computeAttendance = ComputeAttendance()
    private val today = LocalDate.of(2026, 10, 2)

    /** A workout with [total] exercises, the first [finished] of them completed. */
    private fun workout(total: Int, finished: Int) = TrainingDay(
        name = "Push",
        colorHex = "#ff0000",
        exercises = List(total) { Exercise(name = "Exercise $it", completed = it < finished) },
    )

    @Test
    fun `rest day is nothing planned, past or future`() {
        assertEquals(AttendanceLevel.NOTHING_PLANNED, computeAttendance(TrainingDay.REST, today.minusDays(3), today))
        assertEquals(AttendanceLevel.NOTHING_PLANNED, computeAttendance(TrainingDay.REST, today.plusDays(3), today))
    }

    @Test
    fun `future workout is planned even if exercises are marked done`() {
        assertEquals(AttendanceLevel.PLANNED, computeAttendance(workout(4, 4), today.plusDays(1), today))
    }

    @Test
    fun `today and past workouts with nothing finished are missed`() {
        assertEquals(AttendanceLevel.MISSED, computeAttendance(workout(4, 0), today, today))
        assertEquals(AttendanceLevel.MISSED, computeAttendance(workout(4, 0), today.minusDays(1), today))
    }

    @Test
    fun `percentage thresholds match the old app`() {
        assertEquals(AttendanceLevel.LOW, computeAttendance(workout(4, 1), today, today))    // 25%
        assertEquals(AttendanceLevel.MEDIUM, computeAttendance(workout(3, 1), today, today)) // 33%
        assertEquals(AttendanceLevel.MEDIUM, computeAttendance(workout(4, 2), today, today)) // 50%
        assertEquals(AttendanceLevel.HIGH, computeAttendance(workout(3, 2), today, today))   // 66%
        assertEquals(AttendanceLevel.HIGH, computeAttendance(workout(4, 3), today, today))   // 75%
        assertEquals(AttendanceLevel.FULL, computeAttendance(workout(5, 4), today, today))   // 80%
        assertEquals(AttendanceLevel.FULL, computeAttendance(workout(4, 4), today, today))   // 100%
    }
}
