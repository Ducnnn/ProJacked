package com.projacked.app.ui.screens.home

import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.domain.model.TrainingDay
import java.time.LocalDate
import java.time.YearMonth

/** Loading status of the calendar window around the visible month. */
enum class LoadStatus { LOADING, LOADED, ERROR }

/** What the header shows under the date. */
sealed interface TodayWorkout {
    data object Loading : TodayWorkout

    /** No stored workout, or a stored one without exercises. */
    data object Rest : TodayWorkout

    data class Named(val name: String) : TodayWorkout

    /** Reading today's workout failed. Nothing is shown under the date. */
    data object Unavailable : TodayWorkout
}

/** The values on the profile strip, from `users/{uid}`. */
data class ProfileSummary(val age: Int, val heightCm: Int, val weightKg: Double)

/** The read-only day preview that is open. [day] is [TrainingDay.REST] when nothing is stored. */
data class DayPreview(val date: LocalDate, val day: TrainingDay)

/**
 * Everything Home shows.
 *
 * @property levels the heatmap level of every date in a month that has loaded (rest days included).
 * A date of a month that hasn't loaded has no entry, and the calendar draws an empty outline for it.
 * @property days the stored workouts of the loaded months, used by the preview.
 * @property windowStatus the status of the window of months around [visibleMonth].
 * @property profile null while loading, when the document is missing, or after an error: shown as dashes.
 */
data class HomeUiState(
    val today: LocalDate,
    val todayWorkout: TodayWorkout = TodayWorkout.Loading,
    val startMonth: YearMonth,
    val endMonth: YearMonth,
    val visibleMonth: YearMonth,
    val levels: Map<LocalDate, AttendanceLevel> = emptyMap(),
    val days: Map<LocalDate, TrainingDay> = emptyMap(),
    val windowStatus: LoadStatus = LoadStatus.LOADING,
    val preview: DayPreview? = null,
    val profile: ProfileSummary? = null,
)
