package com.projacked.app.domain.model

/** Reasons sign-up credentials are rejected before they are sent to Firebase. */
enum class CredentialsError {
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    PASSWORD_INVALID_CHARACTERS,
}
