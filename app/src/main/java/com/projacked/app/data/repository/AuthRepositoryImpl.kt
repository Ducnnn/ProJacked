package com.projacked.app.data.repository

import com.projacked.app.data.mapper.toAuthError
import com.projacked.app.data.remote.AuthDataSource
import com.projacked.app.domain.model.AuthException
import com.projacked.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository {

    override val signedInUserId: Flow<String?> = authDataSource.userId

    override fun currentUserId(): String? = authDataSource.currentUserId()

    override fun currentUserEmail(): String? = authDataSource.currentUserEmail()

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        resultOf { authDataSource.signIn(email, password) }.asAuthFailure()

    override suspend fun createAccount(email: String, password: String): Result<Unit> =
        resultOf { authDataSource.createAccount(email, password) }.asAuthFailure()

    override fun signOut() = authDataSource.signOut()

    /** Turns any failure into an [AuthException]. Cancellation never gets here: [resultOf] rethrows it. */
    private fun Result<Unit>.asAuthFailure(): Result<Unit> =
        fold(
            onSuccess = { this },
            onFailure = { Result.failure(AuthException(it.toAuthError(), it)) },
        )
}
