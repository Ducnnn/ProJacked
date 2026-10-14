package com.projacked.app.ui.screens.currentday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projacked.app.R
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.SetInputError
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.ui.screens.currentday.components.CurrentDayHeader
import com.projacked.app.ui.screens.currentday.components.ExerciseCard
import com.projacked.app.ui.theme.ProJackedTheme
import java.time.LocalDate

/**
 * Connects [CurrentDayViewModel] to [CurrentDayScreen]. Anything still waiting to be saved is saved when the
 * screen stops (Back, the Home button, switching apps).
 */
@Composable
fun CurrentDayScreen(
    viewModel: CurrentDayViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.flushPendingSave() }
    CurrentDayScreen(
        state = state,
        onRetry = viewModel::retry,
        onRepsChange = viewModel::onRepsChange,
        onWeightChange = viewModel::onWeightChange,
        onSelectSet = viewModel::onSelectSet,
        onAddSet = viewModel::onAddSet,
        onDeleteSet = viewModel::onDeleteSet,
        onFinishedChange = viewModel::onFinishedChange,
        onRetrySave = viewModel::retrySave,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

/** Today's workout: a header, then one card per exercise in one scrolling list. */
@Composable
fun CurrentDayScreen(
    state: CurrentDayUiState,
    onRetry: () -> Unit,
    onRepsChange: (index: Int, text: String) -> Unit,
    onWeightChange: (index: Int, text: String) -> Unit,
    onSelectSet: (index: Int, setIndex: Int) -> Unit,
    onAddSet: (index: Int) -> Unit,
    onDeleteSet: (index: Int) -> Unit,
    onFinishedChange: (index: Int, finished: Boolean) -> Unit,
    onRetrySave: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.logger_save_failed)
    val retryLabel = stringResource(R.string.action_retry)
    LaunchedEffect(state.message) {
        if (state.message == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(message = saveFailed, actionLabel = retryLabel)
        onMessageShown()
        if (result == SnackbarResult.ActionPerformed) onRetrySave()
    }

    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val direction = LocalLayoutDirection.current
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp + innerPadding.calculateStartPadding(direction),
                end = 16.dp + innerPadding.calculateEndPadding(direction),
                top = 16.dp + innerPadding.calculateTopPadding(),
                bottom = 16.dp + innerPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") { CurrentDayHeader(load = state.load, onRetry = onRetry) }
            if (state.load is CurrentDayLoad.Loaded) {
                itemsIndexed(state.cards, key = { index, _ -> index }) { index, card ->
                    ExerciseCard(
                        card = card,
                        onRepsChange = { onRepsChange(index, it) },
                        onWeightChange = { onWeightChange(index, it) },
                        onSelectSet = { onSelectSet(index, it) },
                        onAddSet = { onAddSet(index) },
                        onDeleteSet = { onDeleteSet(index) },
                        onFinishedChange = { onFinishedChange(index, it) },
                    )
                }
            }
        }
    }
}

private val previewDate = LocalDate.of(2026, 10, 12)

@Composable
private fun PreviewScreen(state: CurrentDayUiState) {
    ProJackedTheme {
        CurrentDayScreen(
            state = state,
            onRetry = {},
            onRepsChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onSelectSet = { _, _ -> },
            onAddSet = {},
            onDeleteSet = {},
            onFinishedChange = { _, _ -> },
            onRetrySave = {},
            onMessageShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentDayLoadingPreview() = PreviewScreen(CurrentDayUiState(previewDate))

@Preview(showBackground = true)
@Composable
private fun CurrentDayErrorPreview() = PreviewScreen(CurrentDayUiState(previewDate, CurrentDayLoad.Error))

@Preview(showBackground = true)
@Composable
private fun CurrentDayRestPreview() = PreviewScreen(CurrentDayUiState(previewDate, CurrentDayLoad.Rest))

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun CurrentDayLoadedPreview() {
    val bench = Exercise(
        name = "Bench press",
        muscleGroup = MuscleGroup.CHEST,
        notes = "Pause at the bottom",
        sets = listOf(WorkoutSet(10, 60.0), WorkoutSet(8, 62.5), WorkoutSet()),
    )
    val overhead = Exercise("Overhead press", MuscleGroup.SHOULDERS, sets = listOf(WorkoutSet(8, 22.0), WorkoutSet()))
        .withCompleted(true)
    val long = Exercise(
        name = "Single-arm dumbbell Romanian deadlift with a long name that needs two lines",
        muscleGroup = MuscleGroup.LEGS,
        notes = "Keep the back flat, hinge at the hips, slow on the way down, squeeze at the top. " +
            "Use straps if grip fails before the legs do.",
        sets = listOf(WorkoutSet(12, 20.0), WorkoutSet(10, 24.0), WorkoutSet()),
    )
    PreviewScreen(
        CurrentDayUiState(
            date = previewDate,
            load = CurrentDayLoad.Loaded("Push"),
            cards = listOf(
                ExerciseCardState.of(bench),
                ExerciseCardState.of(overhead).withSelected(0),
                ExerciseCardState.of(long).copy(
                    repsText = "abc",
                    weightText = "1.234",
                    repsError = SetInputError.NOT_WHOLE_NUMBER,
                    weightError = SetInputError.TOO_MANY_DECIMALS,
                ),
            ),
        ),
    )
}
