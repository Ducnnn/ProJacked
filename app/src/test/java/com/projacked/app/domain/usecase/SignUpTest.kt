package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.fakes.FakeAuthRepository
import com.projacked.app.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpTest {

    private val authRepository = FakeAuthRepository()
    private val profileRepository = FakeProfileRepository()
    private val signUp = SignUp(authRepository, profileRepository)

    @Test
    fun `creates the account and a profile with the old app's defaults`() = runTest {
        val result = signUp("new@example.com", "secret1", "Gleb")

        assertTrue(result.isSuccess)
        assertEquals(listOf("new@example.com"), authRepository.createdAccounts)
        val profile = profileRepository.profile.value!!
        assertEquals("new@example.com", profile.email)
        assertEquals("Gleb", profile.displayName)
        assertEquals(BodyParameters.NEW_USER_DEFAULT, profile.parameters)
        assertEquals(MacroGoals.NEW_USER_DEFAULT, profile.macroGoals)
    }

    @Test
    fun `no profile is created when the account can't be created`() = runTest {
        val error = IllegalStateException("email already in use")
        authRepository.createAccountError = error

        val result = signUp("taken@example.com", "secret1", "Gleb")

        assertSame(error, result.exceptionOrNull())
        assertNull(profileRepository.profile.value)
    }
}
