package com.projacked.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.AuthException
import com.projacked.app.domain.model.CredentialsError
import com.projacked.app.domain.usecase.SignUp
import com.projacked.app.domain.usecase.ValidateCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Checks everything on the device first (name not blank, [ValidateCredentials]) and shows every problem at once;
 * only a fully valid form reaches Firebase. Name and email are trimmed, the password never is.
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val validateCredentials: ValidateCredentials,
    private val signUp: SignUp,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) =
        _uiState.update { it.copy(name = name, nameRequired = false, authError = null) }

    fun onEmailChange(email: String) =
        _uiState.update { it.copy(email = email, emailInvalid = false, authError = null) }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, passwordErrors = emptySet(), authError = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return
        val name = current.name.trim()
        val email = current.email.trim()
        val password = current.password
        val credentialsErrors = validateCredentials(email, password)
        if (name.isEmpty() || credentialsErrors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    nameRequired = name.isEmpty(),
                    emailInvalid = CredentialsError.INVALID_EMAIL in credentialsErrors,
                    passwordErrors = credentialsErrors - CredentialsError.INVALID_EMAIL,
                )
            }
            return
        }
        _uiState.update { it.copy(name = name, email = email, isLoading = true, authError = null) }
        viewModelScope.launch {
            signUp(email, password, name).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, signedUp = true) } },
                onFailure = { error ->
                    val authError = (error as? AuthException)?.error ?: AuthError.UNKNOWN
                    _uiState.update { it.copy(isLoading = false, authError = authError) }
                },
            )
        }
    }
}
