package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.AuthRepository
import com.projacked.app.domain.repository.ProfileRepository
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Creates the account, then the profile document with the default parameters (like the old app).
 * Credentials should be checked with [ValidateCredentials] first.
 *
 * Only a failed account creation is a failure. The profile write is attempted for at most 5 seconds and the result
 * is a success either way: on a timeout the write stays in Firestore's queue and may still land, and
 * [EnsureProfileExists] creates the document at the next session start if it never does.
 */
class SignUp @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<Unit> {
        val account = authRepository.createAccount(email, password)
        if (account.isFailure) return account
        withTimeoutOrNull(PROFILE_WRITE_TIMEOUT) {
            profileRepository.createProfile(UserProfile.newUser(email, displayName))
        }
        return Result.success(Unit)
    }

    private companion object {
        val PROFILE_WRITE_TIMEOUT = 5.seconds
    }
}
