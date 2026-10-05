package com.projacked.app.domain.repository

import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** The signed-in user's profile document (`users/{uid}`). */
interface ProfileRepository {

    /** Live profile. Emits null when the document doesn't exist or nobody is signed in. */
    fun observeProfile(): Flow<UserProfile?>

    suspend fun createProfile(profile: UserProfile): Result<Unit>

    /**
     * Creates the document only if it doesn't exist; never overwrites. True when it created it. Fails when offline.
     */
    suspend fun createProfileIfMissing(profile: UserProfile): Result<Boolean>

    /** Saves the body parameters, leaving other profile fields alone. Creates the document if it's missing. */
    suspend fun updateParameters(parameters: BodyParameters): Result<Unit>

    /** Saves the macro goals, leaving other profile fields alone. Creates the document if it's missing. */
    suspend fun updateMacroGoals(goals: MacroGoals): Result<Unit>
}
