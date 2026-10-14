package com.projacked.app.ui.screens.currentday.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.SetInputError
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.components.muscleNameRes
import com.projacked.app.ui.screens.currentday.ExerciseCardState
import com.projacked.app.ui.theme.ProJackedTheme

/** One exercise of today's workout: heading, Finished toggle, set dropdown, "+" and delete, reps and weight. */
@Composable
fun ExerciseCard(
    card: ExerciseCardState,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSelectSet: (Int) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: () -> Unit,
    onFinishedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercise = card.exercise
    val muscle = stringResource(muscleNameRes(exercise.muscleGroup))
    val heading = stringResource(R.string.logger_card_heading, exercise.name, muscle)
    BorderedCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = if (card.finished) {
            ProJackedTheme.extendedColors.exerciseCompleted
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            this.heading()
                            contentDescription = heading
                        },
                )
                FinishedToggle(finished = card.finished, onFinishedChange = onFinishedChange)
            }
            Text(
                text = muscle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (exercise.notes.isNotBlank()) {
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SetDropdown(
                    selected = card.selectedSet,
                    count = exercise.sets.size,
                    onSelect = onSelectSet,
                    modifier = Modifier.weight(1f),
                )
                SetIconButton(R.drawable.ic_add, R.string.logger_add_set, card.canAddSet, onAddSet)
                SetIconButton(R.drawable.ic_delete, R.string.logger_delete_set, card.canDeleteSet, onDeleteSet)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SetField(
                    value = card.repsText,
                    onValueChange = onRepsChange,
                    label = stringResource(R.string.logger_reps),
                    error = repsErrorText(card.repsError),
                    enabled = card.inputsEnabled,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                SetField(
                    value = card.weightText,
                    onValueChange = onWeightChange,
                    label = stringResource(R.string.logger_weight),
                    error = weightErrorText(card.weightError),
                    enabled = card.inputsEnabled,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SetIconButton(iconRes: Int, descriptionRes: Int, enabled: Boolean, onClick: () -> Unit) {
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = content,
            disabledContentColor = content.copy(alpha = 0.38f),
        ),
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = stringResource(descriptionRes))
    }
}

private val previewExercise = Exercise(
    name = "Bench press",
    muscleGroup = MuscleGroup.CHEST,
    notes = "Pause at the bottom",
    sets = listOf(WorkoutSet(10, 60.0), WorkoutSet(8, 62.5), WorkoutSet()),
)

private fun previewCard(
    exercise: Exercise = previewExercise,
    selected: Int = exercise.sets.lastIndex,
) = ExerciseCardState.of(exercise).withSelected(selected)

@Composable
private fun PreviewCard(card: ExerciseCardState) {
    ProJackedTheme {
        ExerciseCard(
            card = card,
            onRepsChange = {},
            onWeightChange = {},
            onSelectSet = {},
            onAddSet = {},
            onDeleteSet = {},
            onFinishedChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseCardPreview() = PreviewCard(previewCard(selected = 1))

@Preview(showBackground = true)
@Composable
private fun ExerciseCardFinishedPreview() =
    PreviewCard(previewCard(previewExercise.withCompleted(true), selected = 1))

@Preview(showBackground = true)
@Composable
private fun ExerciseCardErrorsPreview() = PreviewCard(
    previewCard().copy(
        repsText = "abc",
        weightText = "1.234",
        repsError = SetInputError.NOT_WHOLE_NUMBER,
        weightError = SetInputError.TOO_MANY_DECIMALS,
    ),
)
