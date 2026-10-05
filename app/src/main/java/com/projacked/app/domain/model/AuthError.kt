package com.projacked.app.domain.model

/** Why a sign-in or account creation failed, as far as the person can act on it. */
enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_IN_USE,
    NETWORK,
    TOO_MANY_REQUESTS,
    UNKNOWN,
}
