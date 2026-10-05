package com.projacked.app.ui.screens.auth

import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.CredentialsError

/** What the sign-up screen shows. Every field error can be on at once. */
data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameRequired: Boolean = false,
    val emailInvalid: Boolean = false,
    /** The password rules that failed ([CredentialsError.PASSWORD_TOO_SHORT], [CredentialsError.PASSWORD_INVALID_CHARACTERS]). */
    val passwordErrors: Set<CredentialsError> = emptySet(),
    val authError: AuthError? = null,
    val isLoading: Boolean = false,
    /** Set once when sign-up succeeds; the screen reacts by navigating to Home. */
    val signedUp: Boolean = false,
)
