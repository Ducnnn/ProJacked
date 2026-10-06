package com.projacked.app.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.screens.home.TodayWorkout
import com.projacked.app.ui.theme.ProJackedTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** The date in the old format (`MMM, dd`, device locale, e.g. "Oct, 06") with today's workout under it. */
@Composable
fun HomeHeader(
    date: LocalDate,
    workout: TodayWorkout,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val dateText = remember(date, locale) { date.format(DateTimeFormatter.ofPattern("MMM, dd", locale)) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        val workoutText = when (workout) {
            is TodayWorkout.Named -> workout.name
            TodayWorkout.Rest -> stringResource(R.string.home_rest_day)
            TodayWorkout.Loading, TodayWorkout.Unavailable -> null
        }
        if (workoutText != null) {
            Text(
                text = workoutText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    ProJackedTheme { HomeHeader(date = LocalDate.of(2026, 10, 6), workout = TodayWorkout.Named("Push day")) }
}
