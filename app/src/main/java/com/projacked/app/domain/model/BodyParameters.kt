package com.projacked.app.domain.model

/** The user's body and lifestyle inputs to the macro calculator. */
data class BodyParameters(
    val gender: Gender,
    val heightCm: Int,
    val weightKg: Double,
    val age: Int,
    val activity: ActivityLevel,
    val goal: Goal,
) {
    companion object {
        /** The parameters the old app wrote for every new account. */
        val NEW_USER_DEFAULT = BodyParameters(
            gender = Gender.MALE,
            heightCm = 180,
            weightKg = 80.0,
            age = 18,
            activity = ActivityLevel.AVERAGE,
            goal = Goal.MAINTENANCE,
        )
    }
}
