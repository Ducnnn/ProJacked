package com.projacked.app.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.projacked.app.R
import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.screens.home.LoadStatus
import com.projacked.app.ui.theme.ProJackedTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

private val CellGap = 2.dp
private val CellShape = RoundedCornerShape(2.dp)

/**
 * The attendance heatmap card: a month-and-year title with previous/next buttons, the weekday row, swipeable
 * month pages (always 6 rows) and a legend. [levels] has an entry for every date of a month that has loaded; a
 * date without one draws as an empty outline and can't be tapped. Days of neighbouring months stay blank.
 */
@Composable
fun AttendanceCalendarCard(
    today: LocalDate,
    startMonth: YearMonth,
    endMonth: YearMonth,
    visibleMonth: YearMonth,
    levels: Map<LocalDate, AttendanceLevel>,
    status: LoadStatus,
    onVisibleMonthChanged: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekDays = remember { daysOfWeek() }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = visibleMonth,
        firstDayOfWeek = weekDays.first(),
        outDateStyle = OutDateStyle.EndOfGrid,
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth.yearMonth }.collect(onVisibleMonthChanged)
    }
    val shownMonth = state.firstVisibleMonth.yearMonth

    BorderedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MonthTitleRow(
                month = shownMonth,
                loading = status == LoadStatus.LOADING,
                canGoBack = shownMonth > startMonth,
                canGoForward = shownMonth < endMonth,
                onPrevious = { scope.launch { state.animateScrollToMonth(shownMonth.minusMonths(1)) } },
                onNext = { scope.launch { state.animateScrollToMonth(shownMonth.plusMonths(1)) } },
            )
            WeekdayHeader(weekDays)
            if (status == LoadStatus.ERROR) {
                CalendarError(onRetry)
            } else {
                HorizontalCalendar(
                    state = state,
                    dayContent = { day ->
                        DayCell(day = day, today = today, level = levels[day.date], onDayClick = onDayClick)
                    },
                )
            }
            CalendarLegend()
        }
    }
}

@Composable
private fun MonthTitleRow(
    month: YearMonth,
    loading: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val title = remember(month, locale) {
        month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, enabled = canGoBack, modifier = Modifier.size(48.dp)) {
            Icon(
                painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.home_calendar_previous_month),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        }
        IconButton(onClick = onNext, enabled = canGoForward, modifier = Modifier.size(48.dp)) {
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.home_calendar_next_month),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun WeekdayHeader(weekDays: List<DayOfWeek>) {
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
    day: CalendarDay,
    today: LocalDate,
    level: AttendanceLevel?,
    onDayClick: (LocalDate) -> Unit,
) {
    val cellModifier = Modifier
        .aspectRatio(1f)
        .padding(CellGap)
    if (day.position != DayPosition.MonthDate) {
        Box(cellModifier)
        return
    }
    val colors = ProJackedTheme.extendedColors
    val isToday = day.date == today
    if (level == null) {
        Box(cellModifier.border(BorderStroke(1.dp, colors.attendanceNone), CellShape))
        return
    }
    val levelName = stringResource(levelTextRes(level))
    val dateText = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocalConfiguration.current.locales[0]))
    val description = stringResource(R.string.home_cell_description, dateText, levelName)
        .let { if (isToday) stringResource(R.string.home_cell_today, it) else it }
    Box(
        cellModifier
            .clip4(levelColor(level))
            .then(if (isToday) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground), CellShape) else Modifier)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) { onDayClick(day.date) },
    )
}

private fun Modifier.clip4(color: Color): Modifier = this.background(color, CellShape)

@Composable
private fun levelColor(level: AttendanceLevel): Color {
    val colors = ProJackedTheme.extendedColors
    return when (level) {
        AttendanceLevel.NOTHING_PLANNED -> colors.attendanceNone
        AttendanceLevel.MISSED -> colors.attendanceMissed
        AttendanceLevel.LOW -> colors.attendanceLow
        AttendanceLevel.MEDIUM -> colors.attendanceMedium
        AttendanceLevel.HIGH -> colors.attendanceHigh
        AttendanceLevel.FULL -> colors.attendanceFull
        AttendanceLevel.PLANNED -> colors.attendancePlanned
    }
}

private fun levelTextRes(level: AttendanceLevel): Int = when (level) {
    AttendanceLevel.NOTHING_PLANNED -> R.string.home_level_nothing_planned
    AttendanceLevel.MISSED -> R.string.home_level_missed
    AttendanceLevel.LOW -> R.string.home_level_low
    AttendanceLevel.MEDIUM -> R.string.home_level_medium
    AttendanceLevel.HIGH -> R.string.home_level_high
    AttendanceLevel.FULL -> R.string.home_level_full
    AttendanceLevel.PLANNED -> R.string.home_level_planned
}

@Composable
private fun CalendarError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.home_calendar_error),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CalendarLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        LegendSwatch(AttendanceLevel.NOTHING_PLANNED, stringResource(R.string.home_legend_nothing_planned))
        LegendSwatch(AttendanceLevel.MISSED, stringResource(R.string.home_legend_missed))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendText(stringResource(R.string.home_legend_less))
            for (level in listOf(AttendanceLevel.LOW, AttendanceLevel.MEDIUM, AttendanceLevel.HIGH, AttendanceLevel.FULL)) {
                Swatch(levelColor(level))
            }
            LegendText(stringResource(R.string.home_legend_more))
        }
        LegendSwatch(AttendanceLevel.PLANNED, stringResource(R.string.home_legend_planned))
    }
}

@Composable
private fun LegendSwatch(level: AttendanceLevel, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Swatch(levelColor(level))
        LegendText(text)
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(Modifier.size(12.dp).background(color, CellShape))
}

@Composable
private fun LegendText(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
}

private fun previewLevels(today: LocalDate): Map<LocalDate, AttendanceLevel> {
    val month = YearMonth.from(today)
    val cycle = AttendanceLevel.entries
    return (1..month.lengthOfMonth()).associate { month.atDay(it) to cycle[it % cycle.size] }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceCalendarCardPreview() {
    val today = LocalDate.of(2026, 10, 6)
    ProJackedTheme {
        AttendanceCalendarCard(
            today = today,
            startMonth = YearMonth.of(2026, 1),
            endMonth = YearMonth.of(2027, 1),
            visibleMonth = YearMonth.from(today),
            levels = previewLevels(today),
            status = LoadStatus.LOADED,
            onVisibleMonthChanged = {},
            onDayClick = {},
            onRetry = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceCalendarCardErrorPreview() {
    val today = LocalDate.of(2026, 10, 6)
    ProJackedTheme {
        AttendanceCalendarCard(
            today = today,
            startMonth = YearMonth.of(2026, 1),
            endMonth = YearMonth.of(2027, 1),
            visibleMonth = YearMonth.from(today),
            levels = emptyMap(),
            status = LoadStatus.ERROR,
            onVisibleMonthChanged = {},
            onDayClick = {},
            onRetry = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
