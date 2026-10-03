package com.projacked.app.data.repository

import com.projacked.app.data.remote.AuthDataSource
import com.projacked.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository {

    override val signedInUserId: Flow<String?> = authDataSource.userId

    override fun currentUserId(): String? = authDataSource.currentUserId()

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        resultOf { authDataSource.signIn(email, password) }

    override suspend fun createAccount(email: String, password: String): Result<Unit> =
        resultOf { authDataSource.createAccount(email, password) }

    override fun signOut() = authDataSource.signOut()
}
