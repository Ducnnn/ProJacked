@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.projacked.app.ui.screens.auth

import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.AuthException
import com.projacked.app.domain.model.CredentialsError
import com.projacked.app.domain.usecase.SignUp
import com.projacked.app.domain.usecase.ValidateCredentials
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

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val profileRepository = FakeProfileRepository()
    private val viewModel = SignUpViewModel(ValidateCredentials(), SignUp(authRepository, profileRepository))

    private fun fillValid() {
        viewModel.onNameChange("  Gleb ")
        viewModel.onEmailChange(" me@example.com ")
        viewModel.onPasswordChange("secret1")
    }

    @Test
    fun `a blank name is an error`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onNameChange("   ")
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.nameRequired)
        assertTrue(authRepository.createdAccounts.isEmpty())
    }

    @Test
    fun `every problem shows at once`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("nope")
        viewModel.onPasswordChange("é")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.nameRequired)
        assertTrue(state.emailInvalid)
        assertEquals(setOf(CredentialsError.PASSWORD_TOO_SHORT, CredentialsError.PASSWORD_INVALID_CHARACTERS), state.passwordErrors)
        assertTrue(authRepository.createdAccounts.isEmpty())
    }

    @Test
    fun `valid input signs up with the trimmed name and email`() = runTest(mainDispatcherRule.dispatcher) {
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("me@example.com"), authRepository.createdAccounts)
        assertEquals("Gleb", profileRepository.profile.value!!.displayName)
        assertTrue(viewModel.uiState.value.signedUp)
    }

    @Test
    fun `email in use is shown`() = runTest(mainDispatcherRule.dispatcher) {
        authRepository.createAccountError = AuthException(AuthError.EMAIL_IN_USE)
        fillValid()
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthError.EMAIL_IN_USE, state.authError)
        assertFalse(state.signedUp)
        assertFalse(state.isLoading)
    }

    @Test
    fun `editing a field clears its own errors only`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onEmailChange("nope")
        viewModel.onPasswordChange("é")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.onPasswordChange("secret1")
        assertTrue(viewModel.uiState.value.emailInvalid)
        assertTrue(viewModel.uiState.value.passwordErrors.isEmpty())
        viewModel.onEmailChange("me@example.com")
        assertFalse(viewModel.uiState.value.emailInvalid)
        assertTrue(viewModel.uiState.value.nameRequired)
        viewModel.onNameChange("Gleb")
        assertFalse(viewModel.uiState.value.nameRequired)
    }

    @Test
    fun `a second submit while loading is ignored`() = runTest(mainDispatcherRule.dispatcher) {
        fillValid()
        viewModel.onSubmit()
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, authRepository.createdAccounts.size)
    }
}
