package com.projacked.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.AuthError
import com.projacked.app.domain.model.AuthException
import com.projacked.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sign-in only checks that both fields are filled; Firebase decides the rest (like the old app). The email is
 * trimmed, the password never is.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) =
        _uiState.update { it.copy(email = email, emailRequired = false, authError = null) }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, passwordRequired = false, authError = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return
        val email = current.email.trim()
        val password = current.password
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(emailRequired = email.isEmpty(), passwordRequired = password.isEmpty()) }
            return
        }
        _uiState.update { it.copy(email = email, isLoading = true, authError = null) }
        viewModelScope.launch {
            authRepository.signIn(email, password).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, signedIn = true) } },
                onFailure = { error ->
                    val authError = (error as? AuthException)?.error ?: AuthError.UNKNOWN
                    _uiState.update { it.copy(isLoading = false, authError = authError) }
                },
            )
        }
    }
}
