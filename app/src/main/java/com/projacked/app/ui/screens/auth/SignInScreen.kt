package com.projacked.app.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PlaceholderAction
import com.projacked.app.ui.components.PlaceholderScreen
import com.projacked.app.ui.theme.ProJackedTheme

/** Phase 1 placeholder. Built in Phase 3. */
@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_sign_in),
        modifier = modifier,
        actions = listOf(PlaceholderAction(stringResource(R.string.action_sign_in), onSignedIn)),
    )
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    ProJackedTheme { SignInScreen(onSignedIn = {}) }
}
