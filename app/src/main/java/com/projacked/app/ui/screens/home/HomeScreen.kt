package com.projacked.app.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderAction
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 4. */
@Composable
fun HomeScreen(
    onConstructPlan: () -> Unit,
    onCurrentDay: () -> Unit,
    onMeals: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_home),
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(stringResource(R.string.action_construct_plan), onConstructPlan),
            PlaceholderAction(stringResource(R.string.action_current_day), onCurrentDay),
            PlaceholderAction(stringResource(R.string.action_meals), onMeals),
            PlaceholderAction(stringResource(R.string.action_profile), onProfile),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ProJackedTheme { HomeScreen(onConstructPlan = {}, onCurrentDay = {}, onMeals = {}, onProfile = {}) }
}
