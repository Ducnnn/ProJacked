package com.projacked.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.screens.home.components.AttendanceCalendarCard
import com.projacked.app.ui.screens.home.components.DayPreviewDialog
import com.projacked.app.ui.screens.home.components.HomeHeader
import com.projacked.app.ui.screens.home.components.NutritionCard
import com.projacked.app.ui.screens.home.components.ProfileStrip
import com.projacked.app.ui.screens.home.components.TrainingCard
import com.projacked.app.ui.theme.ProJackedTheme
import java.time.LocalDate
import java.time.YearMonth

/** Connects [HomeViewModel] to [HomeScreen]. */
@Composable
fun HomeScreen(
    onConstructPlan: () -> Unit,
    onCurrentDay: () -> Unit,
    onMeals: () -> Unit,
    onProfile: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshToday() }
    HomeScreen(
        state = state,
        onConstructPlan = onConstructPlan,
        onCurrentDay = onCurrentDay,
        onMeals = onMeals,
        onProfile = onProfile,
        onVisibleMonthChanged = viewModel::onVisibleMonthChanged,
        onDayClick = { date -> if (date == state.today) onCurrentDay() else viewModel.onDayClick(date) },
        onRetry = viewModel::retry,
        onDismissPreview = viewModel::dismissDayPreview,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onConstructPlan: () -> Unit,
    onCurrentDay: () -> Unit,
    onMeals: () -> Unit,
    onProfile: () -> Unit,
    onVisibleMonthChanged: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onRetry: () -> Unit,
    onDismissPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HomeHeader(date = state.today, workout = state.todayWorkout)
            TrainingCard(onConstructPlan = onConstructPlan, onCurrentDay = onCurrentDay)
            NutritionCard(onClick = onMeals)
            AttendanceCalendarCard(
                today = state.today,
                startMonth = state.startMonth,
                endMonth = state.endMonth,
                visibleMonth = state.visibleMonth,
                levels = state.levels,
                status = state.windowStatus,
                onVisibleMonthChanged = onVisibleMonthChanged,
                onDayClick = onDayClick,
                onRetry = onRetry,
            )
            ProfileStrip(profile = state.profile, onProfile = onProfile)
        }
    }
    state.preview?.let { DayPreviewDialog(day = it.day, onDismiss = onDismissPreview) }
}

private val PreviewToday = LocalDate.of(2026, 10, 6)

private fun previewState(
    levels: Map<LocalDate, AttendanceLevel> = AttendanceLevel.entries.let { all ->
        (1..31).associate { YearMonth.from(PreviewToday).atDay(it) to all[it % all.size] }
    },
    status: LoadStatus = LoadStatus.LOADED,
    workout: TodayWorkout = TodayWorkout.Named("Push day"),
    profile: ProfileSummary? = ProfileSummary(18, 180, 80.0),
    preview: DayPreview? = null,
) = HomeUiState(
    today = PreviewToday,
    todayWorkout = workout,
    startMonth = YearMonth.from(PreviewToday).minusMonths(100),
    endMonth = YearMonth.from(PreviewToday).plusMonths(100),
    visibleMonth = YearMonth.from(PreviewToday),
    levels = levels,
    windowStatus = status,
    profile = profile,
    preview = preview,
)

@Composable
private fun PreviewHome(state: HomeUiState) {
    ProJackedTheme {
        HomeScreen(state, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun HomeScreenLoadedPreview() = PreviewHome(previewState())

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun HomeScreenLoadingPreview() = PreviewHome(
    previewState(levels = emptyMap(), status = LoadStatus.LOADING, workout = TodayWorkout.Loading, profile = null),
)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun HomeScreenCalendarErrorPreview() = PreviewHome(
    previewState(levels = emptyMap(), status = LoadStatus.ERROR, workout = TodayWorkout.Unavailable, profile = null),
)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun HomeScreenRestDayPreview() = PreviewHome(previewState(workout = TodayWorkout.Rest))

@Preview(showBackground = true)
@Composable
private fun HomeScreenWithPreviewDialogPreview() = PreviewHome(
    previewState(
        preview = DayPreview(
            PreviewToday.minusDays(1),
            TrainingDay("Push day", "#ff0000", listOf(Exercise(name = "Bench press", completed = true))),
        ),
    ),
)
