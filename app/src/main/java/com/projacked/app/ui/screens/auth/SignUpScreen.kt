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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projacked.app.R
import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.CredentialsError
import com.projacked.app.ui.components.PrimaryButton
import com.projacked.app.ui.screens.auth.components.AuthErrorBox
import com.projacked.app.ui.screens.auth.components.AuthScaffold
import com.projacked.app.ui.screens.auth.components.AuthTextField
import com.projacked.app.ui.screens.auth.components.authErrorMessage
import com.projacked.app.ui.theme.ProJackedTheme

/** Connects [SignUpViewModel] to [SignUpScreen]. */
@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    viewModel: SignUpViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedUp) {
        if (state.signedUp) onSignedUp()
    }
    SignUpScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}

@Composable
fun SignUpScreen(
    state: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordErrors = buildList {
        if (CredentialsError.PASSWORD_TOO_SHORT in state.passwordErrors) {
            add(stringResource(R.string.auth_error_password_too_short))
        }
        if (CredentialsError.PASSWORD_INVALID_CHARACTERS in state.passwordErrors) {
            add(stringResource(R.string.auth_error_password_invalid_characters))
        }
    }

    AuthScaffold(modifier = modifier) {
        Text(
            text = stringResource(R.string.screen_sign_up),
            style = MaterialTheme.typography.displaySmall,
            color = ProJackedTheme.extendedColors.screenTitle,
            textAlign = TextAlign.Center,
        )
        AuthTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.auth_hint_name),
            contentType = ContentType.PersonFullName,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            errors = if (state.nameRequired) listOf(stringResource(R.string.auth_error_name_required)) else emptyList(),
        )
        AuthTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.auth_hint_email),
            contentType = ContentType.EmailAddress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            errors = if (state.emailInvalid) listOf(stringResource(R.string.auth_error_invalid_email)) else emptyList(),
        )
        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.auth_hint_password),
            contentType = ContentType.NewPassword,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            visualTransformation = PasswordVisualTransformation(),
            errors = passwordErrors,
        )
        PrimaryButton(
            text = stringResource(R.string.action_sign_up),
            onClick = onSubmit,
            loading = state.isLoading,
        )
        state.authError?.let {
            AuthErrorBox(message = authErrorMessage(it, R.string.auth_error_sign_up_failed))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenEmptyPreview() {
    ProJackedTheme { SignUpScreen(SignUpUiState(), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenFieldErrorsPreview() {
    ProJackedTheme {
        SignUpScreen(
            SignUpUiState(
                nameRequired = true,
                email = "gleb",
                password = "é",
                emailInvalid = true,
                passwordErrors = setOf(CredentialsError.PASSWORD_INVALID_CHARACTERS, CredentialsError.PASSWORD_TOO_SHORT),
            ),
            {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenLoadingPreview() {
    ProJackedTheme {
        SignUpScreen(
            SignUpUiState(name = "Gleb", email = "gleb@example.com", password = "secret1", isLoading = true),
            {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenEmailInUsePreview() {
    ProJackedTheme {
        SignUpScreen(
            SignUpUiState(
                name = "Gleb",
                email = "gleb@example.com",
                password = "secret1",
                authError = AuthError.EMAIL_IN_USE,
            ),
            {}, {}, {}, {},
        )
    }
}
