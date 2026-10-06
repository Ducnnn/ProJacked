package com.projacked.app.data.remote.dto

import com.google.firebase.firestore.PropertyName

/**
 * A workout as stored in `CustomDays/{name}` (templates) and `TrainingDays/{yyyy-MM-dd}` (assigned dates).
 * Same shape and field names as the old app's `TranDay`.
 */
data class TrainingDayDto(
    var name: String? = null,
    var color: String? = null,
    var exercises: List<ExerciseDto> = emptyList(),
)

data class ExerciseDto(
    var name: String = "",
    /** Muscle group as the old spinner text: "Chest", "Back", "Shoulders", "Arms", "Core" or "Legs". */
    var muscle: String = "",
    var description: String = "",
    var sets: List<WorkoutSetDto> = emptyList(),
    /**
     * The old app's `isCompleted` property, which Firestore stored as `completed` (confirmed in a real
     * TrainingDays document, 2026-10-06).
     */
    @get:PropertyName("completed") @set:PropertyName("completed")
    var completed: Boolean = false,
)

data class WorkoutSetDto(
    var reps: Int = 0,
    /** Kilograms. The old app stored whole numbers; reading them into a Double is safe. */
    var weight: Double = 0.0,
)
