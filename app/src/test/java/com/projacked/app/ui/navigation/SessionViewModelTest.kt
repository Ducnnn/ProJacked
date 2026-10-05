@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.projacked.app.ui.navigation

import com.projacked.app.domain.usecase.EnsureProfileExists
import com.projacked.app.fakes.FakeAuthRepository
import com.projacked.app.fakes.FakeProfileRepository
import com.projacked.app.fakes.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profileRepository = FakeProfileRepository()

    private fun viewModel(authRepository: FakeAuthRepository) =
        SessionViewModel(authRepository, EnsureProfileExists(authRepository, profileRepository))

    @Test
    fun `starts on Home when someone is signed in`() {
        assertEquals(Route.Home, viewModel(FakeAuthRepository(initialUserId = "uid-a")).startRoute)
    }

    @Test
    fun `starts on Welcome when nobody is signed in`() {
        assertEquals(Route.Welcome, viewModel(FakeAuthRepository()).startRoute)
    }

    @Test
    fun `checks the profile once at start for a signed-in user`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel(FakeAuthRepository(initialUserId = "uid-a@example.com"))
        advanceUntilIdle()

        assertEquals(1, profileRepository.createCalls.size)
        assertEquals("a@example.com", profileRepository.createCalls.single().email)
    }

    @Test
    fun `checks again after a sign-in, once per user id`() = runTest(mainDispatcherRule.dispatcher) {
        val auth = FakeAuthRepository()
        viewModel(auth)
        advanceUntilIdle()
        assertTrue(profileRepository.createCalls.isEmpty())

        auth.signIn("a@example.com", "x")
        advanceUntilIdle()
        assertEquals(1, profileRepository.createCalls.size)

        auth.signOut()
        auth.signIn("a@example.com", "x")
        advanceUntilIdle()
        assertEquals(1, profileRepository.createCalls.size)

        auth.signOut()
        auth.signIn("b@example.com", "x")
        advanceUntilIdle()
        assertEquals(2, profileRepository.createCalls.size)
    }

    @Test
    fun `a failed check doesn't crash`() = runTest(mainDispatcherRule.dispatcher) {
        profileRepository.createProfileError = IllegalStateException("offline")
        val viewModel = viewModel(FakeAuthRepository(initialUserId = "uid-a"))
        advanceUntilIdle()

        assertTrue(viewModel.isSignedIn.value)
    }

    @Test
    fun `sign-out shows as signed out`() = runTest(mainDispatcherRule.dispatcher) {
        val auth = FakeAuthRepository(initialUserId = "uid-a")
        val viewModel = viewModel(auth)
        advanceUntilIdle()
        assertTrue(viewModel.isSignedIn.value)

        auth.signOut()
        advanceUntilIdle()

        assertFalse(viewModel.isSignedIn.value)
    }
}
