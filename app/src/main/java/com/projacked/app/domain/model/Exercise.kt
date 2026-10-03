package com.projacked.app.domain.model

/**
 * An exercise in a training day. Always has at least one set; a new exercise starts with one empty set.
 * All editing functions return a new instance (the model is immutable).
 */
data class Exercise(
    val name: String,
    val muscleGroup: MuscleGroup = MuscleGroup.CHEST,
    val notes: String = "",
    val sets: List<WorkoutSet> = listOf(WorkoutSet()),
    val completed: Boolean = false,
) {
    init {
        require(sets.isNotEmpty()) { "An exercise needs at least one set" }
    }

    /** Replaces the set at [index]. */
    fun updateSet(index: Int, set: WorkoutSet): Exercise =
        copy(sets = sets.toMutableList().also { it[index] = set })

    /**
     * The logger's "+" button: saves [set] into the last slot and appends a new empty set.
     * Like the old app, this only works when [index] is the last set and [set] has a value;
     * otherwise the exercise is returned unchanged.
     */
    fun commitSetAndAddNext(index: Int, set: WorkoutSet): Exercise {
        if (index != sets.lastIndex || set.isEmpty) return this
        return copy(sets = sets.dropLast(1) + set + WorkoutSet())
    }

    /** Removes the last set, or clears the only remaining one. */
    fun removeLastSet(): Exercise =
        if (sets.size > 1) copy(sets = sets.dropLast(1)) else copy(sets = listOf(WorkoutSet()))

    fun withCompleted(completed: Boolean): Exercise = copy(completed = completed)

    /** Clears logged values and completion but keeps the number of sets (used when copying a week). */
    fun resetProgress(): Exercise = copy(sets = sets.map { WorkoutSet() }, completed = false)
}
