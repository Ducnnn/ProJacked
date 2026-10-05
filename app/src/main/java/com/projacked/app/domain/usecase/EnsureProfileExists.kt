package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.NotSignedInException
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.AuthRepository
import com.projacked.app.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Creates the signed-in user's `users/{uid}` document with the default values if it's missing, and never touches an
 * existing one. Covers accounts whose profile write failed at sign-up and old accounts without a document.
 * Returns true when it created the document.
 */
class EnsureProfileExists @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<Boolean> {
        if (authRepository.currentUserId() == null) return Result.failure(NotSignedInException())
        val profile = UserProfile.newUser(email = authRepository.currentUserEmail().orEmpty(), displayName = "")
        return profileRepository.createProfileIfMissing(profile)
    }
}
