package com.projacked.app.ui.screens.plan.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.projacked.app.R
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.components.DayRing
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.components.parseTemplateColor
import com.projacked.app.ui.screens.home.LoadStatus
import com.projacked.app.ui.theme.ProJackedTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle

/**
 * The colour of a stored workout's ring: the rest colour for no workout or a rest workout, the parsed stored colour
 * otherwise, and grey when the stored colour can't be read.
 */
@Composable
fun ringColorFor(day: TrainingDay?): Color {
    val colors = ProJackedTheme.extendedColors
    return if (day == null || day.isRest) colors.restDay else parseTemplateColor(day.colorHex) ?: colors.attendanceNone
}

/**
 * The periwinkle week card: a title row with previous/next buttons, the weekday row, a swipeable week of rings and
 * the Copy from previous week button. A date in [loadedDates] shows its ring (rest when it has no entry in [days]);
 * a date that hasn't loaded shows the loading ring and can't be tapped; dates outside the range are blank.
 */
@Composable
fun WeekCard(
    today: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate,
    days: Map<LocalDate, TrainingDay>,
    loadedDates: Set<LocalDate>,
    status: LoadStatus,
    copyRunning: Boolean,
    copyEnabled: Boolean,
    onVisibleWeekChanged: (LocalDate) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onRetry: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekDays = remember { daysOfWeek() }
    val state = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = today,
        firstDayOfWeek = weekDays.first(),
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleWeek.days.first().date }.collect(onVisibleWeekChanged)
    }
    val weekStart = state.firstVisibleWeek.days.first().date
    val locale = LocalConfiguration.current.locales[0]

    BorderedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { scope.launch { state.animateScrollToWeek(weekStart.minusDays(7).coerceAtLeast(startDate)) } },
                    enabled = weekStart > startDate,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chevron_left),
                        contentDescription = stringResource(R.string.plan_previous_week),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = remember(weekStart, locale) { formatWeekTitle(weekStart, locale) },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    if (status == LoadStatus.LOADING) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
                IconButton(
                    onClick = { scope.launch { state.animateScrollToWeek(weekStart.plusDays(7).coerceAtMost(endDate)) } },
                    enabled = weekStart.plusDays(6) < endDate,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chevron_right),
                        contentDescription = stringResource(R.string.plan_next_week),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            WeekdayRow(weekDays)
            if (status == LoadStatus.ERROR) {
                WeekError(onRetry)
            } else {
                WeekCalendar(
                    state = state,
                    dayContent = { day ->
                        DayCell(
                            day = day,
                            today = today,
                            stored = days[day.date],
                            loaded = day.date in loadedDates,
                            onDayClick = onDayClick,
                        )
                    },
                )
            }
            PrimaryButton(
                text = stringResource(R.string.action_copy_previous_week),
                onClick = onCopy,
                enabled = copyEnabled && status != LoadStatus.ERROR,
                loading = copyRunning,
            )
        }
    }
}

@Composable
private fun WeekdayRow(weekDays: List<DayOfWeek>) {
    val locale = LocalConfiguration.current.locales[0]
    Row(Modifier.fillMaxWidth()) {
        for (day in weekDays) {
            Text(
                text = day.getDisplayName(TextStyle.SHORT, locale),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun DayCell(
    day: WeekDay,
    today: LocalDate,
    stored: TrainingDay?,
    loaded: Boolean,
    onDayClick: (LocalDate) -> Unit,
) {
    val cellModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
    if (day.position != WeekDayPosition.RangeDate) {
        Box(cellModifier)
        return
    }
    val isToday = day.date == today
    if (!loaded) {
        Box(cellModifier, contentAlignment = Alignment.Center) {
            DayRing(day.date.dayOfMonth, ringColor = null, isToday = isToday)
        }
        return
    }
    val locale = LocalConfiguration.current.locales[0]
    val workoutName = if (stored == null || stored.isRest) stringResource(R.string.home_rest_day) else stored.name
    val description = stringResource(R.string.plan_cell_description, formatPlanDate(day.date, locale), workoutName)
        .let { if (isToday) stringResource(R.string.plan_cell_today, it) else it }
    Box(
        cellModifier
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) { onDayClick(day.date) },
        contentAlignment = Alignment.Center,
    ) {
        DayRing(day.date.dayOfMonth, ringColor = ringColorFor(stored), isToday = isToday)
    }
}

@Composable
private fun WeekError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.plan_week_error),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeekCardPreview() {
    val today = LocalDate.of(2026, 10, 6)
    ProJackedTheme {
        WeekCard(
            today = today,
            startDate = LocalDate.of(2026, 1, 1),
            endDate = LocalDate.of(2027, 1, 31),
            days = mapOf(
                today.minusDays(1) to TrainingDay("Push", "#e53935"),
                today to TrainingDay("Legs", "#FFFFFF"),
                today.plusDays(1) to TrainingDay("Odd", "red"),
                today.plusDays(2) to TrainingDay("pull", "#1E88E5"),
            ),
            loadedDates = (-10L..10L).map { today.plusDays(it) }.toSet(),
            status = LoadStatus.LOADED,
            copyRunning = false,
            copyEnabled = true,
            onVisibleWeekChanged = {},
            onDayClick = {},
            onRetry = {},
            onCopy = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
