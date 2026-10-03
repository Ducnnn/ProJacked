package com.projacked.app.data.mapper

import com.projacked.app.data.remote.dto.ExerciseDto
import com.projacked.app.data.remote.dto.TrainingDayDto
import com.projacked.app.data.remote.dto.WorkoutSetDto
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet

/** Missing name or colour fall back to the rest day's values, like the old `TranDay` constructor. */
internal fun TrainingDayDto.toDomain(): TrainingDay = TrainingDay(
    name = name ?: TrainingDay.REST_NAME,
    colorHex = color ?: TrainingDay.REST_COLOR_HEX,
    exercises = exercises.map { it.toDomain() },
)

internal fun TrainingDay.toDto(): TrainingDayDto = TrainingDayDto(
    name = name,
    color = colorHex,
    exercises = exercises.map { it.toDto() },
)

/** An exercise stored without sets gets one empty set, so every exercise has at least one. */
internal fun ExerciseDto.toDomain(): Exercise = Exercise(
    name = name,
    muscleGroup = parseMuscleGroup(muscle),
    notes = description,
    sets = sets.map { it.toDomain() }.ifEmpty { listOf(WorkoutSet()) },
    completed = completed,
)

internal fun Exercise.toDto(): ExerciseDto = ExerciseDto(
    name = name,
    muscle = muscleGroup.storageName,
    description = notes,
    sets = sets.map { it.toDto() },
    completed = completed,
)

internal fun WorkoutSetDto.toDomain() = WorkoutSet(reps = reps, weightKg = weight)

internal fun WorkoutSet.toDto() = WorkoutSetDto(reps = reps, weight = weightKg)

/** Muscle groups are stored as the old spinner text ("Chest", "Back", …) to stay compatible. */
internal val MuscleGroup.storageName: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

/** Case-insensitive. Unknown values fall back to the old app's default, Chest. */
internal fun parseMuscleGroup(raw: String?): MuscleGroup =
    MuscleGroup.entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: MuscleGroup.CHEST
