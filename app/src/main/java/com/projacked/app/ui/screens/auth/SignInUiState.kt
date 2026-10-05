package com.projacked.app.ui.screens.auth

import com.projacked.app.domain.model.AuthError

/** What the sign-in screen shows. */
data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailRequired: Boolean = false,
    val passwordRequired: Boolean = false,
    val authError: AuthError? = null,
    val isLoading: Boolean = false,
    /** Set once when sign-in succeeds; the screen reacts by navigating to Home. */
    val signedIn: Boolean = false,
)
