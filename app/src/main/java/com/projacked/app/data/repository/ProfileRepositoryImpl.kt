package com.projacked.app.data.repository

import com.projacked.app.data.mapper.UserFields
import com.projacked.app.data.mapper.toDomain
import com.projacked.app.data.mapper.toDto
import com.projacked.app.data.mapper.toFieldMap
import com.projacked.app.data.remote.AuthDataSource
import com.projacked.app.data.remote.FirestoreDataSource
import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.NotSignedInException
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
) : ProfileRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<UserProfile?> =
        authDataSource.userId.flatMapLatest { uid ->
            if (uid == null) flowOf(null) else firestoreDataSource.observeUser(uid).map { it?.toDomain() }
        }

    override suspend fun createProfile(profile: UserProfile): Result<Unit> = resultOf {
        firestoreDataSource.setUser(requireUid(), profile.toDto())
    }

    override suspend fun updateParameters(parameters: BodyParameters): Result<Unit> = resultOf {
        firestoreDataSource.mergeUser(requireUid(), parameters.toFieldMap())
    }

    override suspend fun updateMacroGoals(goals: MacroGoals): Result<Unit> = resultOf {
        firestoreDataSource.mergeUser(requireUid(), mapOf(UserFields.MACRO_GOALS to goals.toDto()))
    }

    private fun requireUid(): String = authDataSource.currentUserId() ?: throw NotSignedInException()
}
