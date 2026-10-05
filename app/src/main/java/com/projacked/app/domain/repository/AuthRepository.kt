package com.projacked.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Email-and-password authentication. */
interface AuthRepository {

    /** The signed-in user's id, or null when signed out. Emits again on every sign-in or sign-out. */
    val signedInUserId: Flow<String?>

    /** The signed-in user's id right now, or null. */
    fun currentUserId(): String?

    /** The signed-in user's email right now, or null. */
    fun currentUserEmail(): String?

    /** Fails with [com.projacked.app.domain.model.AuthException] when Firebase rejects the sign-in. */
    suspend fun signIn(email: String, password: String): Result<Unit>

    /**
     * Creates the Firebase Auth account and signs it in. Doesn't create the profile document.
     * Fails with [com.projacked.app.domain.model.AuthException].
     */
    suspend fun createAccount(email: String, password: String): Result<Unit>

    fun signOut()
}
