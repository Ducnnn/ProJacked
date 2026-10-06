package com.projacked.app.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.screens.home.ProfileSummary
import com.projacked.app.ui.theme.ProJackedTheme

/** Age, height and weight from the profile ("–" while unknown), and a Profile button. */
@Composable
fun ProfileStrip(
    profile: ProfileSummary?,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val unknown = stringResource(R.string.home_profile_unknown)
    BorderedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ProfileValue(
                    label = stringResource(R.string.home_profile_age),
                    value = profile?.age?.toString() ?: unknown,
                )
                ProfileValue(
                    label = stringResource(R.string.home_profile_height),
                    value = profile?.let { stringResource(R.string.home_profile_height_value, it.heightCm.toString()) }
                        ?: unknown,
                )
                ProfileValue(
                    label = stringResource(R.string.home_profile_weight),
                    value = profile?.let { stringResource(R.string.home_profile_weight_value, formatWeight(it.weightKg, locale)) }
                        ?: unknown,
                )
            }
            Button(
                onClick = onProfile,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(R.string.action_profile), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ProfileValue(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStripPreview() {
    ProJackedTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileStrip(profile = ProfileSummary(age = 18, heightCm = 180, weightKg = 80.0), onProfile = {})
            ProfileStrip(profile = null, onProfile = {})
        }
    }
}
