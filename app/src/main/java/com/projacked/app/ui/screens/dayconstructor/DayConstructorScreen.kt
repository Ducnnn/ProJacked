package com.projacked.app.ui.screens.dayconstructor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projacked.app.R
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.usecase.TemplateNameError
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.dayconstructor.components.ColorButton
import com.projacked.app.ui.screens.dayconstructor.components.ColorDialog
import com.projacked.app.ui.screens.dayconstructor.components.ConfirmDiscardDialog
import com.projacked.app.ui.screens.dayconstructor.components.ConfirmReplaceDialog
import com.projacked.app.ui.screens.dayconstructor.components.ExerciseDialog
import com.projacked.app.ui.screens.dayconstructor.components.ExercisePanel
import com.projacked.app.ui.screens.dayconstructor.components.NameField
import com.projacked.app.ui.screens.plan.TemplatesState
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * Connects [DayConstructorViewModel] to [DayConstructorScreen]. [onSaved] runs once after Save; [onBack] runs
 * once after the user confirms discarding the draft.
 */
@Composable
fun DayConstructorScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: DayConstructorViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(state.discarded) { if (state.discarded) onBack() }
    BackHandler(enabled = state.hasChanges && state.dialog == null && !state.saved && !state.discarded) {
        viewModel.onBackWithChanges()
    }
    DayConstructorScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onAddExercise = viewModel::onAddExerciseClick,
        onExerciseClick = viewModel::onExerciseClick,
        onDeleteExercise = viewModel::onDeleteExercise,
        onColorClick = viewModel::onColorClick,
        onSave = viewModel::onSaveClick,
        onRetryTemplates = viewModel::retryTemplates,
        onEditorNameChange = viewModel::onEditorNameChange,
        onEditorMuscleChange = viewModel::onEditorMuscleChange,
        onEditorNotesChange = viewModel::onEditorNotesChange,
        onConfirmExercise = viewModel::confirmExercise,
        onColorPicked = viewModel::onColorPicked,
        onConfirmReplace = viewModel::confirmReplace,
        onConfirmDiscard = viewModel::confirmDiscard,
        onDismissDialog = viewModel::dismissDialog,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

@Composable
fun DayConstructorScreen(
    state: DayConstructorUiState,
    onNameChange: (String) -> Unit,
    onAddExercise: () -> Unit,
    onExerciseClick: (Int) -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onColorClick: () -> Unit,
    onSave: () -> Unit,
    onRetryTemplates: () -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorMuscleChange: (MuscleGroup) -> Unit,
    onEditorNotesChange: (String) -> Unit,
    onConfirmExercise: () -> Unit,
    onColorPicked: (String) -> Unit,
    onConfirmReplace: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onDismissDialog: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.dc_save_failed)
    LaunchedEffect(state.message) {
        if (state.message == DayConstructorMessage.SAVE_FAILED) {
            snackbarHostState.showSnackbar(saveFailed)
            onMessageShown()
        }
    }
    val colorName = TemplatePalette.firstOrNull { it.hex == state.colorHex }
        ?.let { stringResource(it.nameRes) } ?: state.colorHex

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.templates is TemplatesState.Error) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.dc_templates_error),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = onRetryTemplates) {
                            Text(stringResource(R.string.action_retry), color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ColorButton(colorHex = state.colorHex, colorName = colorName, onClick = onColorClick)
                    SaveButton(
                        loading = state.templates is TemplatesState.Loading,
                        enabled = state.templates is TemplatesState.Loaded && !state.saved,
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { NameField(value = state.name, error = state.nameError, onValueChange = onNameChange) }
            item { PrimaryButton(text = stringResource(R.string.action_add_exercise), onClick = onAddExercise) }
            item {
                ExercisePanel(
                    exercises = state.exercises,
                    onExerciseClick = onExerciseClick,
                    onDeleteExercise = onDeleteExercise,
                )
            }
        }
    }

    when (val dialog = state.dialog) {
        is ConstructorDialog.ExerciseEditor -> ExerciseDialog(
            editor = dialog,
            onNameChange = onEditorNameChange,
            onMuscleChange = onEditorMuscleChange,
            onNotesChange = onEditorNotesChange,
            onConfirm = onConfirmExercise,
            onDismiss = onDismissDialog,
        )
        ConstructorDialog.ColorPicker -> ColorDialog(
            selectedHex = state.colorHex,
            onPick = onColorPicked,
            onDismiss = onDismissDialog,
        )
        is ConstructorDialog.ConfirmReplace -> ConfirmReplaceDialog(
            name = dialog.name,
            onConfirm = onConfirmReplace,
            onDismiss = onDismissDialog,
        )
        ConstructorDialog.ConfirmDiscard -> ConfirmDiscardDialog(
            onConfirm = onConfirmDiscard,
            onDismiss = onDismissDialog,
        )
        null -> Unit
    }
}

/** The purple "Save workout" button. Shows a progress indicator while the saved workouts load. */
@Composable
private fun SaveButton(
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 64.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = if (loading) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            },
            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary,
            )
        } else {
            Text(stringResource(R.string.action_save_workout), style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val previewExercises = listOf(
    Exercise("Bench press", MuscleGroup.CHEST),
    Exercise("Overhead press", MuscleGroup.SHOULDERS),
    Exercise("A very long exercise name that does not fit on one line at all", MuscleGroup.ARMS),
)

private val previewLoaded = TemplatesState.Loaded(listOf(TrainingDay("Push", "#e53935")))

@Composable
private fun PreviewScreen(state: DayConstructorUiState) {
    ProJackedTheme {
        DayConstructorScreen(
            state = state,
            onNameChange = {}, onAddExercise = {}, onExerciseClick = {}, onDeleteExercise = {},
            onColorClick = {}, onSave = {}, onRetryTemplates = {}, onEditorNameChange = {},
            onEditorMuscleChange = {}, onEditorNotesChange = {}, onConfirmExercise = {}, onColorPicked = {},
            onConfirmReplace = {}, onConfirmDiscard = {}, onDismissDialog = {}, onMessageShown = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DayConstructorLoadingPreview() = PreviewScreen(DayConstructorUiState())

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DayConstructorFilledPreview() = PreviewScreen(
    DayConstructorUiState(
        name = "Upper body",
        colorHex = "#00897b",
        exercises = previewExercises,
        templates = previewLoaded,
    ),
)

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DayConstructorNameErrorPreview() = PreviewScreen(
    DayConstructorUiState(name = "Push/Pull", nameError = TemplateNameError.CONTAINS_SLASH, templates = previewLoaded),
)

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DayConstructorTemplatesErrorPreview() = PreviewScreen(
    DayConstructorUiState(name = "Legs", templates = TemplatesState.Error),
)

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DayConstructorNoExercisesPreview() = PreviewScreen(
    DayConstructorUiState(name = "Cardio", templates = previewLoaded),
)
