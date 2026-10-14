package com.projacked.app.ui.screens.currentday

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.SetInputError
import com.projacked.app.domain.model.WorkoutSet
import java.math.BigDecimal
import java.time.LocalDate

/** What the logger is showing. */
sealed interface CurrentDayLoad {
    data object Loading : CurrentDayLoad

    /** Today's workout couldn't be read; Retry loads it again. */
    data object Error : CurrentDayLoad

    /** No document, a stored rest day, or a workout without exercises (P7-10). */
    data object Rest : CurrentDayLoad

    data class Loaded(val workoutName: String) : CurrentDayLoad
}

/** A one-off message for the screen to show, then clear with `onMessageShown()`. */
enum class CurrentDayMessage { SAVE_FAILED }

/**
 * One exercise card. [exercise] holds the saved values; [repsText] and [weightText] are what the user typed for
 * the set at [selectedSet]. A field with an error keeps its text, while [exercise] keeps the last valid value.
 */
data class ExerciseCardState(
    val exercise: Exercise,
    val selectedSet: Int,
    val repsText: String,
    val weightText: String,
    val repsError: SetInputError? = null,
    val weightError: SetInputError? = null,
) {
    val finished: Boolean get() = exercise.completed

    /** Reps, weight and the two buttons are locked while finished (P7-7). */
    val inputsEnabled: Boolean get() = !finished

    /** "+" works on the last set, when it has a value and no field has an error (P7-6). */
    val canAddSet: Boolean
        get() = inputsEnabled && selectedSet == exercise.sets.lastIndex &&
            repsError == null && weightError == null && !exercise.sets[selectedSet].isEmpty

    /** Delete works unless the card is finished or holds a single empty set. */
    val canDeleteSet: Boolean
        get() = inputsEnabled && !(exercise.sets.size == 1 && exercise.sets[0].isEmpty)

    /** This card with both fields reloaded from the set at [set] and their errors cleared. */
    fun withSelected(set: Int): ExerciseCardState = copy(
        selectedSet = set,
        repsText = formatReps(exercise.sets[set]),
        weightText = formatWeight(exercise.sets[set]),
        repsError = null,
        weightError = null,
    )

    companion object {
        /** A card opening on the exercise's last set (P7-5). */
        fun of(exercise: Exercise): ExerciseCardState =
            ExerciseCardState(exercise, 0, "", "").withSelected(exercise.sets.lastIndex)
    }
}

data class CurrentDayUiState(
    val date: LocalDate,
    val load: CurrentDayLoad = CurrentDayLoad.Loading,
    val cards: List<ExerciseCardState> = emptyList(),
    val message: CurrentDayMessage? = null,
)

/** Reps as typed text: a 0 is an empty field (the screen shows the "0" placeholder). */
internal fun formatReps(set: WorkoutSet): String = if (set.reps == 0) "" else set.reps.toString()

/** Weight as typed text, without a trailing ".0" and with "." in every locale ("60", "62.5"); 0 is empty. */
internal fun formatWeight(set: WorkoutSet): String =
    if (set.weightKg == 0.0) "" else BigDecimal.valueOf(set.weightKg).stripTrailingZeros().toPlainString()
