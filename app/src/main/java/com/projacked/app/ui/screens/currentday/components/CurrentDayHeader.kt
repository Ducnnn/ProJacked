package com.projacked.app.ui.screens.currentday.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.currentday.CurrentDayLoad
import com.projacked.app.ui.theme.ProJackedTheme

/** The "Current day" pill, with what the screen is showing under it: the workout's name, or its loading, rest or error state. */
@Composable
fun CurrentDayHeader(
    load: CurrentDayLoad,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val white = ProJackedTheme.extendedColors.screenTitle
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BorderedCard {
            Text(
                text = stringResource(R.string.screen_current_day),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        when (load) {
            CurrentDayLoad.Loading -> CircularProgressIndicator(color = white)
            is CurrentDayLoad.Loaded -> Text(
                text = load.workoutName,
                style = MaterialTheme.typography.headlineSmall,
                color = white,
                textAlign = TextAlign.Center,
            )
            CurrentDayLoad.Rest -> {
                Text(
                    text = stringResource(R.string.home_rest_day),
                    style = MaterialTheme.typography.headlineSmall,
                    color = white,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.logger_nothing_to_log),
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    textAlign = TextAlign.Center,
                )
            }
            CurrentDayLoad.Error -> {
                Text(
                    text = stringResource(R.string.logger_load_error),
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(text = stringResource(R.string.action_retry), onClick = onRetry)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF78B0F0)
@Composable
private fun CurrentDayHeaderLongNamePreview() {
    ProJackedTheme {
        CurrentDayHeader(
            load = CurrentDayLoad.Loaded("Upper body strength and hypertrophy, heavy day with accessories"),
            onRetry = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
