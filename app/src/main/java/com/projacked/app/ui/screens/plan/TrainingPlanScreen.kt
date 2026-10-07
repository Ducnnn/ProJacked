package com.projacked.app.ui.screens.plan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.home.LoadStatus
import com.projacked.app.ui.screens.plan.components.PanelFooter
import com.projacked.app.ui.screens.plan.components.PanelHeader
import com.projacked.app.ui.screens.plan.components.PanelMessage
import com.projacked.app.ui.screens.plan.components.PlanDialogHost
import com.projacked.app.ui.screens.plan.components.WeekCard
import com.projacked.app.ui.screens.plan.components.WorkoutRow
import com.projacked.app.ui.theme.ProJackedTheme
import java.time.LocalDate

/** Connects [TrainingPlanViewModel] to [TrainingPlanScreen]. */
@Composable
fun TrainingPlanScreen(
    onAddWorkout: () -> Unit,
    viewModel: TrainingPlanViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshToday() }
    TrainingPlanScreen(
        state = state,
        onAddWorkout = onAddWorkout,
        onVisibleWeekChanged = viewModel::onVisibleWeekChanged,
        onDayClick = viewModel::onDayClick,
        onRetryWeek = viewModel::retryWeek,
        onRetryTemplates = viewModel::retryTemplates,
        onCopy = viewModel::onCopyClick,
        onChoose = viewModel::onChoose,
        onConfirmReplace = viewModel::confirmReplace,
        onConfirmCopy = viewModel::confirmCopy,
        onConfirmDelete = viewModel::confirmDelete,
        onTemplateLongPress = viewModel::onTemplateLongPress,
        onDismissDialog = viewModel::dismissDialog,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

/**
 * The Training plan: a week card on top, then the lilac "List of workouts" panel, all in one scrolling list.
 * "Add new workout" stays pinned at the bottom. There is no top bar, as in the old app.
 */
@Composable
fun TrainingPlanScreen(
    state: TrainingPlanUiState,
    onAddWorkout: () -> Unit,
    onVisibleWeekChanged: (LocalDate) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onRetryWeek: () -> Unit,
    onRetryTemplates: () -> Unit,
    onCopy: () -> Unit,
    onChoose: (TrainingDay) -> Unit,
    onConfirmReplace: () -> Unit,
    onConfirmCopy: () -> Unit,
    onConfirmDelete: () -> Unit,
    onTemplateLongPress: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = state.message?.let { messageText(it) }
    LaunchedEffect(state.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(Modifier.navigationBarsPadding().padding(16.dp)) {
                PrimaryButton(text = stringResource(R.string.action_add_workout), onClick = onAddWorkout)
            }
        },
    ) { innerPadding ->
        val direction = LocalLayoutDirection.current
        LazyColumn(
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(direction) + 16.dp,
                end = innerPadding.calculateEndPadding(direction) + 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp,
            ),
        ) {
            item(key = "week") {
                WeekCard(
                    today = state.today,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    days = state.days,
                    loadedDates = state.loadedDates,
                    status = state.windowStatus,
                    copyRunning = state.copyRunning,
                    copyEnabled = state.visibleWeekLoaded,
                    onVisibleWeekChanged = onVisibleWeekChanged,
                    onDayClick = onDayClick,
                    onRetry = onRetryWeek,
                    onCopy = onCopy,
                )
                Spacer(Modifier.height(12.dp))
            }
            item(key = "header") { PanelHeader() }
            val templates = state.templates
            if (templates is TemplatesState.Loaded && templates.templates.isNotEmpty()) {
                items(templates.templates, key = { "template:${it.name}" }) { workout ->
                    WorkoutRow(workout = workout, onLongPress = { onTemplateLongPress(workout.name) })
                }
            } else {
                item(key = "message") { PanelMessage(templates, onRetryTemplates) }
            }
            item(key = "footer") { PanelFooter() }
        }
    }

    PlanDialogHost(
        dialog = state.dialog,
        days = state.days,
        templates = state.templates,
        onChoose = onChoose,
        onRetryTemplates = onRetryTemplates,
        onConfirmReplace = onConfirmReplace,
        onConfirmCopy = onConfirmCopy,
        onConfirmDelete = onConfirmDelete,
        onDismiss = onDismissDialog,
    )
}

@Composable
private fun messageText(message: PlanMessage): String = when (message) {
    PlanMessage.Copied -> stringResource(R.string.plan_copied)
    PlanMessage.CopyFailed -> stringResource(R.string.plan_copy_failed)
    PlanMessage.SaveFailed -> stringResource(R.string.plan_save_failed)
    is PlanMessage.DeleteFailed -> stringResource(R.string.plan_delete_failed, message.templateName)
}

private val PreviewToday = LocalDate.of(2026, 10, 6)

private val PreviewTemplates = listOf(
    TrainingDay("Legs", "#FFFFFF", listOf(Exercise("Squat"))),
    TrainingDay("pull", "#1E88E5", listOf(Exercise("Barbell row"))),
    TrainingDay("Push", "#e53935", listOf(Exercise("Bench press"))),
    TrainingDay("Upper body", "#8e24aa"),
)

private fun previewState(
    status: LoadStatus = LoadStatus.LOADED,
    templates: TemplatesState = TemplatesState.Loaded(PreviewTemplates),
    copyRunning: Boolean = false,
    loaded: Boolean = true,
) = TrainingPlanUiState(
    today = PreviewToday,
    startDate = PreviewToday.minusMonths(6),
    endDate = PreviewToday.plusMonths(6),
    visibleWeekStart = PreviewToday.minusDays(1),
    days = mapOf(
        PreviewToday.minusDays(1) to PreviewTemplates[2],
        PreviewToday to PreviewTemplates[0],
        PreviewToday.plusDays(1) to TrainingDay("Odd colour", "red", listOf(Exercise("Squat"))),
        PreviewToday.plusDays(2) to PreviewTemplates[1],
        PreviewToday.plusDays(3) to TrainingDay.REST,
    ),
    loadedDates = if (loaded) (-20L..20L).map { PreviewToday.plusDays(it) }.toSet() else emptySet(),
    windowStatus = status,
    templates = templates,
    copyRunning = copyRunning,
)

@Composable
private fun PreviewPlan(state: TrainingPlanUiState) {
    ProJackedTheme {
        TrainingPlanScreen(state, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TrainingPlanLoadedPreview() = PreviewPlan(previewState())

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TrainingPlanLoadingPreview() =
    PreviewPlan(previewState(status = LoadStatus.LOADING, templates = TemplatesState.Loading, loaded = false))

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TrainingPlanWeekErrorPreview() =
    PreviewPlan(previewState(status = LoadStatus.ERROR, templates = TemplatesState.Error))

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TrainingPlanNoWorkoutsPreview() =
    PreviewPlan(previewState(templates = TemplatesState.Loaded(emptyList())))

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TrainingPlanCopyRunningPreview() = PreviewPlan(previewState(copyRunning = true))
