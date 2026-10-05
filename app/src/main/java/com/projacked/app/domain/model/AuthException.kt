package com.projacked.app.domain.model

/** A failed sign-in or account creation, classified as an [AuthError]. */
class AuthException(val error: AuthError, cause: Throwable? = null) : Exception("Auth failed: $error", cause)
