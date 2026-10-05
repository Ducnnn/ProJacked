package com.projacked.app.ui.screens.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.projacked.app.R
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.auth.components.AuthScaffold
import com.projacked.app.ui.theme.ProJackedTheme

/** First screen for signed-out people: the title and the two ways in. */
@Composable
fun WelcomeScreen(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScaffold(modifier = modifier) {
        Text(
            text = stringResource(R.string.screen_welcome),
            style = MaterialTheme.typography.displayMedium,
            color = ProJackedTheme.extendedColors.screenTitle,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(text = stringResource(R.string.action_sign_in), onClick = onSignIn)
        PrimaryButton(text = stringResource(R.string.action_sign_up), onClick = onSignUp)
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    ProJackedTheme { WelcomeScreen(onSignIn = {}, onSignUp = {}) }
}
