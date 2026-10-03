package com.projacked.app.domain.model

/**
 * A workout: used both for templates (`CustomDays`) and for the copy assigned to a date (`TrainingDays`).
 *
 * A rest day is represented by [REST]. In storage, a rest day is simply a date with no document.
 *
 * @property colorHex the template's colour as `#RRGGBB`, shown as the date ring on the training plan.
 */
data class TrainingDay(
    val name: String,
    val colorHex: String,
    val exercises: List<Exercise> = emptyList(),
) {
    /** Matches the old app's rule: a day named "Rest", with the rest colour and no exercises. */
    val isRest: Boolean
        get() = name == REST_NAME && colorHex.equals(REST_COLOR_HEX, ignoreCase = true) && exercises.isEmpty()

    /** Clears every exercise's logged values and completion, keeping the number of sets. */
    fun resetProgress(): TrainingDay = copy(exercises = exercises.map { it.resetProgress() })

    companion object {
        const val REST_NAME = "Rest"
        const val REST_COLOR_HEX = "#ffa9a3"

        val REST = TrainingDay(name = REST_NAME, colorHex = REST_COLOR_HEX)
    }
}
