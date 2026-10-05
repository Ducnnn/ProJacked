@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.projacked.app.ui.screens.auth

import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.AuthException
import com.projacked.app.fakes.FakeAuthRepository
import com.projacked.app.fakes.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = SignInViewModel(authRepository)

    @Test
    fun `blank fields show required errors and send nothing`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("   ")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.emailRequired)
        assertTrue(state.passwordRequired)
        assertFalse(state.isLoading)
        assertTrue(authRepository.signedInEmails.isEmpty())
    }

    @Test
    fun `the email is trimmed and the password is not`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("  me@example.com ")
        viewModel.onPasswordChange(" secret ")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("me@example.com"), authRepository.signedInEmails)
    }

    @Test
    fun `success sets the signed-in flag`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("secret")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.signedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `failure shows the auth error`() = runTest(mainDispatcherRule.dispatcher) {
        authRepository.signInError = AuthException(AuthError.INVALID_CREDENTIALS)
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("wrong")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthError.INVALID_CREDENTIALS, state.authError)
        assertFalse(state.signedIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `an error that isn't an AuthException shows as unknown`() = runTest(mainDispatcherRule.dispatcher) {
        authRepository.signInError = IllegalStateException("boom")
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("secret")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthError.UNKNOWN, viewModel.uiState.value.authError)
    }

    @Test
    fun `editing a field clears its error and the auth error`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onEmailChange("m")
        assertFalse(viewModel.uiState.value.emailRequired)
        assertTrue(viewModel.uiState.value.passwordRequired)
        viewModel.onPasswordChange("p")
        assertFalse(viewModel.uiState.value.passwordRequired)

        authRepository.signInError = AuthException(AuthError.NETWORK)
        viewModel.onSubmit()
        advanceUntilIdle()
        assertEquals(AuthError.NETWORK, viewModel.uiState.value.authError)
        viewModel.onPasswordChange("pp")
        assertNull(viewModel.uiState.value.authError)
    }

    @Test
    fun `a second submit while loading is ignored`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("secret")
        viewModel.onSubmit()
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, authRepository.signedInEmails.size)
    }
}
