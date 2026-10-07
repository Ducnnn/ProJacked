package com.projacked.app.ui.screens.plan

import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.screens.home.LoadStatus
import java.time.LocalDate

/** The workout templates list: loading, loaded (sorted by name, ignoring case) or failed. */
sealed interface TemplatesState {
    data object Loading : TemplatesState
    data class Loaded(val templates: List<TrainingDay>) : TemplatesState
    data object Error : TemplatesState
}

/** The dialog that is open, if any. Lives in the state so it survives rotation. */
sealed interface PlanDialog {
    /** Pick Rest or a workout for [date]. */
    data class Assign(val date: LocalDate) : PlanDialog

    /** [date] has logged progress; asks before replacing it with [chosen]. */
    data class ConfirmReplace(val date: LocalDate, val chosen: TrainingDay) : PlanDialog

    /** The week starting on [weekStart] already has a workout; asks before copying over it. */
    data class ConfirmCopy(val weekStart: LocalDate) : PlanDialog

    data class ConfirmDelete(val templateName: String) : PlanDialog
}

/** A one-off message for the snackbar. The screen shows it and calls `onMessageShown()`. */
sealed interface PlanMessage {
    data object Copied : PlanMessage
    data object CopyFailed : PlanMessage
    data object SaveFailed : PlanMessage
    data class DeleteFailed(val templateName: String) : PlanMessage
}

/**
 * Everything the Training plan shows.
 *
 * @property visibleWeekStart the first date of the week the strip shows; null until the screen reports it.
 * @property days the stored workouts of the dates loaded so far. A loaded date with no entry is a rest day.
 * @property loadedDates the dates a window has already emitted for.
 * @property windowStatus the status of the window around [visibleWeekStart].
 */
data class TrainingPlanUiState(
    val today: LocalDate,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val visibleWeekStart: LocalDate? = null,
    val days: Map<LocalDate, TrainingDay> = emptyMap(),
    val loadedDates: Set<LocalDate> = emptySet(),
    val windowStatus: LoadStatus = LoadStatus.LOADING,
    val templates: TemplatesState = TemplatesState.Loading,
    val dialog: PlanDialog? = null,
    val copyRunning: Boolean = false,
    val message: PlanMessage? = null,
) {
    /** True when all 7 dates of the visible week have loaded. */
    val visibleWeekLoaded: Boolean
        get() = visibleWeekStart?.let { start -> (0L until 7L).all { start.plusDays(it) in loadedDates } } == true
}
