package com.projacked.app.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.theme.ProJackedTheme

/** Two purple buttons of equal width: the training plan and the logger. */
@Composable
fun TrainingCard(
    onConstructPlan: () -> Unit,
    onCurrentDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BorderedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TrainingButton(R.string.action_construct_plan, onConstructPlan, Modifier.weight(1f))
            TrainingButton(R.string.action_current_day, onCurrentDay, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TrainingButton(label: Int, onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    ) {
        Text(text = stringResource(label), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainingCardPreview() {
    ProJackedTheme { TrainingCard(onConstructPlan = {}, onCurrentDay = {}) }
}
