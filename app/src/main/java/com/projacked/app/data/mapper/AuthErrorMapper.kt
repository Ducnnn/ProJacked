package com.projacked.app.data.mapper

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.projacked.app.domain.model.AuthError

private const val ERROR_USER_DISABLED = "ERROR_USER_DISABLED"

/** Maps a Firebase Auth failure to the [AuthError] shown to the person. */
internal fun Throwable.toAuthError(): AuthError = when (this) {
    // Before its parent class, FirebaseAuthInvalidCredentialsException.
    is FirebaseAuthWeakPasswordException -> AuthError.UNKNOWN
    is FirebaseAuthInvalidUserException ->
        if (errorCode == ERROR_USER_DISABLED) AuthError.UNKNOWN else AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseNetworkException -> AuthError.NETWORK
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
    else -> AuthError.UNKNOWN
}
