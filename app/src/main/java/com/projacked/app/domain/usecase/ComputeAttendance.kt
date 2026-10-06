package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.domain.model.TrainingDay
import java.time.LocalDate
import javax.inject.Inject

/**
 * Heatmap level for one date, with the old app's thresholds.
 *
 * One change from the old app (P4-4): today with nothing finished is [AttendanceLevel.PLANNED], not
 * [AttendanceLevel.MISSED], because the day isn't over yet. Past days with nothing finished are still missed.
 */
class ComputeAttendance @Inject constructor() {

    operator fun invoke(day: TrainingDay, date: LocalDate, today: LocalDate): AttendanceLevel {
        val exercises = day.exercises
        if (exercises.isEmpty()) return AttendanceLevel.NOTHING_PLANNED
        if (date.isAfter(today)) return AttendanceLevel.PLANNED

        val finished = exercises.count { it.completed }
        if (finished == 0 && date == today) return AttendanceLevel.PLANNED

        val percentFinished = (finished.toDouble() / exercises.size * 100).toInt()
        return when (percentFinished) {
            in 1..25 -> AttendanceLevel.LOW
            in 26..50 -> AttendanceLevel.MEDIUM
            in 51..75 -> AttendanceLevel.HIGH
            in 76..100 -> AttendanceLevel.FULL
            else -> AttendanceLevel.MISSED
        }
    }
}
