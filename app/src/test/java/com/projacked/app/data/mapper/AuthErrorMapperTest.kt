package com.projacked.app.data.mapper

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.projacked.app.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun `weak password is unknown, not invalid credentials`() {
        assertEquals(AuthError.UNKNOWN, FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "too short").toAuthError())
    }

    @Test
    fun `disabled user is unknown`() {
        assertEquals(AuthError.UNKNOWN, FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "disabled").toAuthError())
    }

    @Test
    fun `other invalid users are invalid credentials`() {
        assertEquals(AuthError.INVALID_CREDENTIALS, FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "no user").toAuthError())
    }

    @Test
    fun `invalid credentials`() {
        assertEquals(AuthError.INVALID_CREDENTIALS, FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "wrong").toAuthError())
    }

    @Test
    fun `email collision`() {
        assertEquals(AuthError.EMAIL_IN_USE, FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "taken").toAuthError())
    }

    @Test
    fun `network failure`() {
        assertEquals(AuthError.NETWORK, FirebaseNetworkException("offline").toAuthError())
    }

    @Test
    fun `too many requests`() {
        assertEquals(AuthError.TOO_MANY_REQUESTS, FirebaseTooManyRequestsException("slow down").toAuthError())
    }

    @Test
    fun `anything else is unknown`() {
        assertEquals(AuthError.UNKNOWN, IllegalStateException("boom").toAuthError())
    }
}
