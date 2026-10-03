package com.projacked.app.domain.model

/** The signed-in user's profile document (`users/{uid}`). */
data class UserProfile(
    val email: String,
    val displayName: String,
    val parameters: BodyParameters,
    val macroGoals: MacroGoals,
) {
    companion object {
        /** A profile for a freshly created account, with the same defaults as the old app. */
        fun newUser(email: String, displayName: String) = UserProfile(
            email = email,
            displayName = displayName,
            parameters = BodyParameters.NEW_USER_DEFAULT,
            macroGoals = MacroGoals.NEW_USER_DEFAULT,
        )
    }
}
