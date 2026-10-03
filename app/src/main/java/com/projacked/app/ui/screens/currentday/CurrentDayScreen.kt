package com.projacked.app.ui.screens.currentday

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 7. */
@Composable
fun CurrentDayScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_current_day),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CurrentDayScreenPreview() {
    ProJackedTheme { CurrentDayScreen() }
}
