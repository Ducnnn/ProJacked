package com.projacked.app.ui.screens.auth.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.AuthError
import com.projacked.app.ui.theme.ProJackedTheme

/** The Firebase failure under the button: a small white rounded box with red text. */
@Composable
fun AuthErrorBox(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
        )
    }
}

/** The message for [error]; [unknownMessage] is the sign-in or sign-up wording for [AuthError.UNKNOWN]. */
@Composable
fun authErrorMessage(error: AuthError, @StringRes unknownMessage: Int): String = stringResource(
    when (error) {
        AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthError.EMAIL_IN_USE -> R.string.auth_error_email_in_use
        AuthError.NETWORK -> R.string.auth_error_network
        AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
        AuthError.UNKNOWN -> unknownMessage
    },
)

@Preview(showBackground = true)
@Composable
private fun AuthErrorBoxPreview() {
    ProJackedTheme { AuthErrorBox(message = stringResource(R.string.auth_error_invalid_credentials)) }
}
