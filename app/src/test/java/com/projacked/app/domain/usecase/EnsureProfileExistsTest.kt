package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.NotSignedInException
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.fakes.FakeAuthRepository
import com.projacked.app.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureProfileExistsTest {

    private val authRepository = FakeAuthRepository(initialUserId = "uid-me@example.com")
    private val profileRepository = FakeProfileRepository()
    private val ensureProfileExists = EnsureProfileExists(authRepository, profileRepository)

    @Test
    fun `a missing profile is created with the account's email, a blank name and the defaults`() = runTest {
        val result = ensureProfileExists()

        assertEquals(true, result.getOrNull())
        val profile = profileRepository.profile.value!!
        assertEquals("me@example.com", profile.email)
        assertEquals("", profile.displayName)
        assertEquals(BodyParameters.NEW_USER_DEFAULT, profile.parameters)
        assertEquals(MacroGoals.NEW_USER_DEFAULT, profile.macroGoals)
    }

    @Test
    fun `an existing profile is left untouched`() = runTest {
        val existing = UserProfile.newUser("me@example.com", "Gleb")
        profileRepository.profile.value = existing

        val result = ensureProfileExists()

        assertEquals(false, result.getOrNull())
        assertSame(existing, profileRepository.profile.value)
    }

    @Test
    fun `a write failure is reported`() = runTest {
        val error = IllegalStateException("offline")
        profileRepository.createProfileError = error

        val result = ensureProfileExists()

        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `nobody signed in fails with NotSignedInException and writes nothing`() = runTest {
        authRepository.signOut()

        val result = ensureProfileExists()

        assertTrue(result.exceptionOrNull() is NotSignedInException)
        assertFalse(profileRepository.createCalls.isNotEmpty())
    }
}
