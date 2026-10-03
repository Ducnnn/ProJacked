package com.projacked.app.fakes

import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [ProfileRepository] holding a single profile. */
class FakeProfileRepository : ProfileRepository {
    val profile = MutableStateFlow<UserProfile?>(null)

    override fun observeProfile(): Flow<UserProfile?> = profile

    override suspend fun createProfile(profile: UserProfile): Result<Unit> {
        this.profile.value = profile
        return Result.success(Unit)
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
