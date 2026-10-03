package com.projacked.app.ui.screens.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderAction
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 5. */
@Composable
fun TrainingPlanScreen(
    onAddWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_training_plan),
        modifier = modifier,
        actions = listOf(PlaceholderAction(stringResource(R.string.action_add_workout), onAddWorkout)),
    )
}

@Preview(showBackground = true)
@Composable
private fun TrainingPlanScreenPreview() {
    ProJackedTheme { TrainingPlanScreen(onAddWorkout = {}) }
}
