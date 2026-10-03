package com.projacked.app.fakes

import com.projacked.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [AuthRepository]. Set [createAccountError] to make account creation fail. */
class FakeAuthRepository : AuthRepository {
    private val userId = MutableStateFlow<String?>(null)
    var createAccountError: Exception? = null
    val createdAccounts = mutableListOf<String>()

    override val signedInUserId: Flow<String?> = userId

    override fun currentUserId(): String? = userId.value

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        userId.value = "uid-$email"
        return Result.success(Unit)
    }

    override suspend fun createAccount(email: String, password: String): Result<Unit> {
        createAccountError?.let { return Result.failure(it) }
        createdAccounts += email
        userId.value = "uid-$email"
        return Result.success(Unit)
    }

    override fun signOut() {
        userId.value = null
    }
}
