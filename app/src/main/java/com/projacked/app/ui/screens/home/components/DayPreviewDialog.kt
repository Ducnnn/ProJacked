package com.projacked.app.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.ui.theme.CardBorderWidth
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * Read-only look at one day's workout (replaces the old app's editable logger card). Sets with no reps and no
 * weight are skipped: the old logger leaves an empty one at the end of every logged exercise.
 */
@Composable
fun DayPreviewDialog(
    day: TrainingDay,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasExercises = day.exercises.isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        title = {
            Text(
                text = if (hasExercises) day.name else stringResource(R.string.home_rest_day),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        text = {
            if (!hasExercises) {
                Text(
                    text = stringResource(R.string.home_nothing_planned),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(day.exercises) { ExerciseBlock(it) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun ExerciseBlock(exercise: Exercise) {
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer
    val locale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = textColor)
        Text(stringResource(muscleNameRes(exercise.muscleGroup)), style = MaterialTheme.typography.bodyMedium, color = textColor)
        if (exercise.completed) {
            Text(stringResource(R.string.home_preview_finished), style = MaterialTheme.typography.labelLarge, color = textColor)
        }
        if (exercise.notes.isNotBlank()) {
            Text(exercise.notes, style = MaterialTheme.typography.bodyMedium, color = textColor)
        }
        val sets = exercise.sets.withIndex().filterNot { it.value.isEmpty }
        if (sets.isEmpty()) {
            Text(stringResource(R.string.home_preview_no_sets), style = MaterialTheme.typography.bodyMedium, color = textColor)
        } else {
            for ((index, set) in sets) {
                Text(
                    text = stringResource(
                        R.string.home_preview_set,
                        index + 1,
                        pluralStringResource(R.plurals.home_reps, set.reps, set.reps),
                        formatWeight(set.weightKg, locale),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
            }
        }
    }
}

private fun muscleNameRes(group: MuscleGroup): Int = when (group) {
    MuscleGroup.CHEST -> R.string.muscle_chest
    MuscleGroup.BACK -> R.string.muscle_back
    MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
    MuscleGroup.ARMS -> R.string.muscle_arms
    MuscleGroup.CORE -> R.string.muscle_core
    MuscleGroup.LEGS -> R.string.muscle_legs
}

@Preview(showBackground = true)
@Composable
private fun DayPreviewDialogPreview() {
    ProJackedTheme {
        DayPreviewDialog(
            day = TrainingDay(
                name = "Push day",
                colorHex = "#ff0000",
                exercises = listOf(
                    Exercise(
                        name = "Bench press",
                        muscleGroup = MuscleGroup.CHEST,
                        notes = "Slow negatives",
                        completed = true,
                        sets = listOf(WorkoutSet(10, 60.0), WorkoutSet(1, 22.5), WorkoutSet()),
                    ),
                    Exercise(name = "Curls", muscleGroup = MuscleGroup.ARMS),
                ),
            ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayPreviewDialogRestPreview() {
    ProJackedTheme { DayPreviewDialog(day = TrainingDay.REST, onDismiss = {}) }
}
