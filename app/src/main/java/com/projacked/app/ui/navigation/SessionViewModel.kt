package com.projacked.app.ui.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.repository.AuthRepository
import com.projacked.app.domain.usecase.EnsureProfileExists
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Who is signed in. Decides the start destination once (auto-login, no splash) and makes sure every signed-in
 * user has a profile document, once per user id for the life of this ViewModel. The check is silent: it only logs.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val ensureProfileExists: EnsureProfileExists,
) : ViewModel() {

    private val initialUserId = authRepository.currentUserId()

    val startRoute: Route = if (initialUserId != null) Route.Home else Route.Welcome

    /** Changes on every sign-in and sign-out, including when Firebase ends the session itself. */
    private val _isSignedIn = MutableStateFlow(initialUserId != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val checkedUserIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            authRepository.signedInUserId.collect { uid ->
                _isSignedIn.value = uid != null
                // Launched on its own so a slow or offline check never delays a sign-out.
                if (uid != null && checkedUserIds.add(uid)) launch { checkProfile() }
            }
        }
    }

    private suspend fun checkProfile() {
        ensureProfileExists().fold(
            onSuccess = { created -> Log.i(TAG, "Profile check done, created=$created") },
            onFailure = { error -> Log.w(TAG, "Profile check failed; it runs again at the next start", error) },
        )
    }

    private companion object {
        const val TAG = "SessionViewModel"
    }
}
