package com.projacked.app.ui.screens.dayconstructor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.projacked.app.R
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.ui.components.muscleNameRes
import com.projacked.app.ui.screens.dayconstructor.ConstructorDialog
import com.projacked.app.ui.screens.dayconstructor.TemplatePalette
import com.projacked.app.ui.theme.ProJackedTheme

/** Adds or edits an exercise. A tap outside doesn't close it, so typing isn't lost by accident. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDialog(
    editor: ConstructorDialog.ExerciseEditor,
    onNameChange: (String) -> Unit,
    onMuscleChange: (MuscleGroup) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                stringResource(
                    if (editor.index == null) R.string.dc_exercise_title_new else R.string.dc_exercise_title_edit,
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.dc_exercise_name)) },
                    singleLine = true,
                    isError = editor.showNameError,
                    supportingText = if (editor.showNameError) {
                        { Text(stringResource(R.string.dc_exercise_error_name)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
                    OutlinedTextField(
                        value = stringResource(muscleNameRes(editor.muscleGroup)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.dc_exercise_muscle)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        MuscleGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(stringResource(muscleNameRes(group))) },
                                onClick = {
                                    onMuscleChange(group)
                                    menuOpen = false
                                },
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.dc_exercise_notes)) },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(if (editor.index == null) R.string.action_add else R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** The 4×4 colour grid. Tapping a swatch picks it. */
@Composable
fun ColorDialog(
    selectedHex: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.dc_color_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TemplatePalette.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        rowColors.forEach { color ->
                            ColorSwatch(
                                hex = color.hex,
                                name = stringResource(color.nameRes),
                                selected = color.hex == selectedHex,
                                onClick = { onPick(color.hex) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Asks before replacing a saved workout of the same name. */
@Composable
fun ConfirmReplaceDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.dc_replace_title, name), maxLines = 2) },
        text = { Text(stringResource(R.string.dc_replace_text, name)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_replace)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Asks before throwing an unsaved draft away. */
@Composable
fun ConfirmDiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.dc_discard_title)) },
        text = { Text(stringResource(R.string.dc_discard_text)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_discard)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_keep_editing)) } },
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDialogNewPreview() {
    ProJackedTheme {
        ExerciseDialog(
            ConstructorDialog.ExerciseEditor(null, "", MuscleGroup.CHEST, "", showNameError = true),
            {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDialogEditPreview() {
    ProJackedTheme {
        ExerciseDialog(
            ConstructorDialog.ExerciseEditor(1, "Bench press", MuscleGroup.SHOULDERS, "Slow"),
            {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorDialogDarkPreview() {
    ProJackedTheme { ColorDialog(selectedHex = TemplatePalette[8].hex, onPick = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ColorDialogLightPreview() {
    ProJackedTheme { ColorDialog(selectedHex = TemplatePalette[11].hex, onPick = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmReplaceDialogPreview() {
    ProJackedTheme { ConfirmReplaceDialog(name = "Push", onConfirm = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDiscardDialogPreview() {
    ProJackedTheme { ConfirmDiscardDialog(onConfirm = {}, onDismiss = {}) }
}
