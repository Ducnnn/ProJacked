package com.projacked.app.fakes

import com.projacked.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [AuthRepository]. Set [signInError] or [createAccountError] to make those calls fail, and
 * [emailOverride] to change the account's email.
 */
class FakeAuthRepository(initialUserId: String? = null) : AuthRepository {
    private val userId = MutableStateFlow(initialUserId)
    var signInError: Exception? = null
    var createAccountError: Exception? = null
    var emailOverride: String? = null
    val signedInEmails = mutableListOf<String>()
    val createdAccounts = mutableListOf<String>()
    var signOutCount = 0
        private set

    override val signedInUserId: Flow<String?> = userId

    override fun currentUserId(): String? = userId.value

    override fun currentUserEmail(): String? = emailOverride ?: userId.value?.removePrefix("uid-")

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        signInError?.let { return Result.failure(it) }
        signedInEmails += email
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
        signOutCount++
        userId.value = null
    }
}
