package com.projacked.app.ui.screens.meals

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderAction
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 8. */
@Composable
fun MealsScreen(
    onAddMeal: () -> Unit,
    onSuggestedNutrition: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_meals),
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(stringResource(R.string.action_add_meal), onAddMeal),
            PlaceholderAction(stringResource(R.string.action_suggested_nutrition), onSuggestedNutrition),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun MealsScreenPreview() {
    ProJackedTheme { MealsScreen(onAddMeal = {}, onSuggestedNutrition = {}) }
}
