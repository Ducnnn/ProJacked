package com.projacked.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.projacked.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Phase 9 adds the profile values; for now it only logs out. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** Signs out. Navigation back to Welcome follows from the session state, not from here. */
    fun logOut() = authRepository.signOut()
}
