package com.projacked.app.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderAction
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 9. */
@Composable
fun ProfileScreen(
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_profile),
        modifier = modifier,
        actions = listOf(PlaceholderAction(stringResource(R.string.action_log_out), onLogOut)),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProJackedTheme { ProfileScreen(onLogOut = {}) }
}
