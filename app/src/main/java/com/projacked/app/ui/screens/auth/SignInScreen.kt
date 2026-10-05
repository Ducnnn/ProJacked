package com.projacked.app.ui.screens.auth

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projacked.app.R
import com.projacked.app.domain.model.AuthError
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.auth.components.AuthErrorBox
import com.projacked.app.ui.screens.auth.components.AuthScaffold
import com.projacked.app.ui.screens.auth.components.AuthTextField
import com.projacked.app.ui.screens.auth.components.authErrorMessage
import com.projacked.app.ui.theme.ProJackedTheme

/** Connects [SignInViewModel] to [SignInScreen]. */
@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    viewModel: SignInViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedIn) {
        if (state.signedIn) onSignedIn()
    }
    SignInScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}

@Composable
fun SignInScreen(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScaffold(modifier = modifier) {
        Text(
            text = stringResource(R.string.screen_sign_in),
            style = MaterialTheme.typography.displaySmall,
            color = ProJackedTheme.extendedColors.screenTitle,
            textAlign = TextAlign.Center,
        )
        AuthTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.auth_hint_email),
            contentType = ContentType.EmailAddress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            errors = if (state.emailRequired) listOf(stringResource(R.string.auth_error_email_required)) else emptyList(),
        )
        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.auth_hint_password),
            contentType = ContentType.Password,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            visualTransformation = PasswordVisualTransformation(),
            errors = if (state.passwordRequired) listOf(stringResource(R.string.auth_error_password_required)) else emptyList(),
        )
        PrimaryButton(
            text = stringResource(R.string.action_sign_in),
            onClick = onSubmit,
            loading = state.isLoading,
        )
        state.authError?.let {
            AuthErrorBox(message = authErrorMessage(it, R.string.auth_error_sign_in_failed))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenEmptyPreview() {
    ProJackedTheme { SignInScreen(SignInUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenFieldErrorsPreview() {
    ProJackedTheme {
        SignInScreen(SignInUiState(emailRequired = true, passwordRequired = true), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenLoadingPreview() {
    ProJackedTheme {
        SignInScreen(SignInUiState(email = "gleb@example.com", password = "secret1", isLoading = true), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenAuthErrorPreview() {
    ProJackedTheme {
        SignInScreen(
            SignInUiState(email = "gleb@example.com", password = "wrong", authError = AuthError.INVALID_CREDENTIALS),
            {}, {}, {},
        )
    }
}
