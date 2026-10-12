package com.projacked.app.ui.screens.dayconstructor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.usecase.TemplateNameError
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.components.muscleNameRes
import com.projacked.app.ui.components.parseTemplateColor
import com.projacked.app.ui.screens.dayconstructor.TemplatePalette
import com.projacked.app.ui.theme.CardBorderWidth
import com.projacked.app.ui.theme.ProJackedTheme

/** The workout name: a white box (so the red error stays readable), heavy centred text, one line. */
@Composable
fun NameField(
    value: String,
    error: TemplateNameError?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                placeholder = {
                    Text(
                        text = stringResource(R.string.dc_name_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
            )
            if (error != null) {
                Text(
                    text = stringResource(nameErrorRes(error)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        }
    }
}

private fun nameErrorRes(error: TemplateNameError): Int = when (error) {
    TemplateNameError.BLANK -> R.string.dc_error_name_blank
    TemplateNameError.CONTAINS_SLASH -> R.string.dc_error_name_slash
    TemplateNameError.RESERVED, TemplateNameError.TOO_LONG -> R.string.dc_error_name_reserved
}

/** The periwinkle "Exercises" panel: header, then one row per exercise, or the empty message. */
@Composable
fun ExercisePanel(
    exercises: List<Exercise>,
    onExerciseClick: (Int) -> Unit,
    onDeleteExercise: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BorderedCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dc_exercises_header),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (exercises.isEmpty()) {
            Text(
                text = stringResource(R.string.dc_exercises_empty),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            exercises.forEachIndexed { index, exercise ->
                ExerciseRow(
                    exercise = exercise,
                    onClick = { onExerciseClick(index) },
                    onDelete = { onDeleteExercise(index) },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** An exercise: the name on a lilac pill with the muscle group under it, and a bin button. Tapping edits it. */
@Composable
fun ExerciseRow(
    exercise: Exercise,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val muscle = stringResource(muscleNameRes(exercise.muscleGroup))
    val rowDescription = stringResource(R.string.dc_exercise_row_description, exercise.name, muscle)
    val deleteDescription = stringResource(R.string.dc_exercise_delete_description, exercise.name)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .heightIn(min = 48.dp)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = rowDescription
                },
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = exercise.name,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = muscle,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = deleteDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** The square button showing the chosen colour, with the old drafting-compass icon. */
@Composable
fun ColorButton(
    colorHex: String,
    colorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.dc_color_button_description, colorName)
    val color = parseTemplateColor(colorHex) ?: MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .border(CardBorderWidth, ProJackedTheme.extendedColors.cardBorder, MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_architecture),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** One colour in the picker grid: 48dp, with a check mark and an outline when selected. */
@Composable
fun ColorSwatch(
    hex: String,
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = parseTemplateColor(hex) ?: Color.Gray
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface), MaterialTheme.shapes.small)
                } else {
                    Modifier
                },
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConstructorFieldsPreview() {
    ProJackedTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NameField(value = "Push/Pull", error = TemplateNameError.CONTAINS_SLASH, onValueChange = {})
            ExercisePanel(
                exercises = listOf(
                    Exercise("Bench press", MuscleGroup.CHEST),
                    Exercise("A very long exercise name that does not fit on a single line", MuscleGroup.SHOULDERS),
                ),
                onExerciseClick = {},
                onDeleteExercise = {},
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorButton(TemplatePalette[8].hex, "Teal", onClick = {})
                ColorSwatch(TemplatePalette[11].hex, "Lime", selected = true, onClick = {})
                ColorSwatch(TemplatePalette[2].hex, "Purple", selected = true, onClick = {})
            }
        }
    }
}
