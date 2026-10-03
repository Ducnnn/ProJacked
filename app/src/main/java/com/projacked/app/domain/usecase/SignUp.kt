package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.AuthRepository
import com.projacked.app.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Creates the account, then the profile document with the default parameters (like the old app).
 * Credentials should be checked with [ValidateCredentials] first.
 */
class SignUp @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<Unit> =
        authRepository.createAccount(email, password).fold(
            onSuccess = { profileRepository.createProfile(UserProfile.newUser(email, displayName)) },
            onFailure = { Result.failure(it) },
        )
}
