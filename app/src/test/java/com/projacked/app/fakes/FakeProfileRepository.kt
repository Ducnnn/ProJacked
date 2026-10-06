package com.projacked.app.fakes

import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * In-memory [ProfileRepository] holding a single profile. Set [createProfileError] to make writes fail and
 * [createDelayMillis] to make them slow; every attempt is recorded in [createCalls].
 */
class FakeProfileRepository : ProfileRepository {
    val profile = MutableStateFlow<UserProfile?>(null)
    var createProfileError: Exception? = null
    var createDelayMillis: Long = 0
    val createCalls = mutableListOf<UserProfile>()

    /** When set, [observeProfile] fails as soon as it is collected. */
    var observeError: Exception? = null

    override fun observeProfile(): Flow<UserProfile?> =
        observeError?.let { error -> flow { throw error } } ?: profile

    override suspend fun createProfile(profile: UserProfile): Result<Unit> {
        createCalls += profile
        if (createDelayMillis > 0) delay(createDelayMillis)
        createProfileError?.let { return Result.failure(it) }
        this.profile.value = profile
        return Result.success(Unit)
    }

    override suspend fun createProfileIfMissing(profile: UserProfile): Result<Boolean> {
        createCalls += profile
        createProfileError?.let { return Result.failure(it) }
        if (this.profile.value != null) return Result.success(false)
        this.profile.value = profile
        return Result.success(true)
    }

    override suspend fun updateParameters(parameters: BodyParameters): Result<Unit> = update {
        it.copy(parameters = parameters)
    }

    override suspend fun updateMacroGoals(goals: MacroGoals): Result<Unit> = update { it.copy(macroGoals = goals) }

    private fun update(change: (UserProfile) -> UserProfile): Result<Unit> {
        val current = profile.value ?: return Result.failure(NoSuchElementException("No profile"))
        profile.value = change(current)
        return Result.success(Unit)
    }
}
