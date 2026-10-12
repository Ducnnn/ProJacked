package com.projacked.app.ui.components

import androidx.annotation.StringRes
import com.projacked.app.R
import com.projacked.app.domain.model.MuscleGroup

/** The display name of a muscle group, shared by every screen that shows one. */
@StringRes
fun muscleNameRes(group: MuscleGroup): Int = when (group) {
    MuscleGroup.CHEST -> R.string.muscle_chest
    MuscleGroup.BACK -> R.string.muscle_back
    MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
    MuscleGroup.ARMS -> R.string.muscle_arms
    MuscleGroup.CORE -> R.string.muscle_core
    MuscleGroup.LEGS -> R.string.muscle_legs
}
