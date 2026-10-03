package com.projacked.app.data.mapper

import com.projacked.app.data.remote.dto.MacroGoalsDto
import com.projacked.app.data.remote.dto.UserDto
import com.projacked.app.domain.model.ActivityLevel
import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.Gender
import com.projacked.app.domain.model.Goal
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.UserProfile

// Field names of users/{uid}, used for partial updates.
internal object UserFields {
    const val GENDER = "gender"
    const val HEIGHT = "height"
    const val WEIGHT = "weight"
    const val AGE = "age"
    const val ACTIVITY = "activity"
    const val GOAL = "goal"
    const val MACRO_GOALS = "macroGoals"
}

internal fun UserDto.toDomain(): UserProfile = UserProfile(
    email = email,
    displayName = displayName,
    parameters = BodyParameters(
        gender = parseGender(gender),
        heightCm = height,
        weightKg = weight,
        age = age,
        activity = parseActivityLevel(activity),
        goal = parseGoal(goal),
    ),
    macroGoals = macroGoals?.toDomain() ?: MacroGoals.NEW_USER_DEFAULT,
)

/** For creating the document. `createdAt` is left null so Firestore fills in the server time. */
internal fun UserProfile.toDto(): UserDto = UserDto(
    email = email,
    displayName = displayName,
    gender = parameters.gender.storageKey,
    height = parameters.heightCm,
    weight = parameters.weightKg,
    age = parameters.age,
    activity = parameters.activity.storageKey,
    goal = parameters.goal.storageKey,
    macroGoals = macroGoals.toDto(),
)

/** For a partial update of users/{uid} that leaves email, name, createdAt and macro goals alone. */
internal fun BodyParameters.toFieldMap(): Map<String, Any> = mapOf(
    UserFields.GENDER to gender.storageKey,
    UserFields.HEIGHT to heightCm,
    UserFields.WEIGHT to weightKg,
    UserFields.AGE to age,
    UserFields.ACTIVITY to activity.storageKey,
    UserFields.GOAL to goal.storageKey,
)

internal fun MacroGoalsDto.toDomain() = MacroGoals(protein = protein, fats = fats, carbs = carbs, calories = calories)

internal fun MacroGoals.toDto() = MacroGoalsDto(protein = protein, fats = fats, carbs = carbs, calories = calories)

// Stored text values. ProJacked writes the lowercase keys below and reads every variant the old app wrote.

internal val Gender.storageKey: String
    get() = when (this) {
        Gender.MALE -> "male"
        Gender.FEMALE -> "female"
    }

internal val ActivityLevel.storageKey: String
    get() = when (this) {
        ActivityLevel.SEDENTARY -> "sedentary"
        ActivityLevel.LIGHT -> "light"
        ActivityLevel.AVERAGE -> "average"
        ActivityLevel.ACTIVE -> "active"
    }

internal val Goal.storageKey: String
    get() = when (this) {
        Goal.CUT -> "cut"
        Goal.BULK -> "bulk"
        Goal.MAINTENANCE -> "maintenance"
    }

/** Reads "male", "Male", "female", "Female". Anything else falls back to the old app's default, male. */
internal fun parseGender(raw: String?): Gender =
    if (raw?.trim().equals("female", ignoreCase = true)) Gender.FEMALE else Gender.MALE

/**
 * Reads the new keys, the old default "average", and the old spinner labels ("Little-No Exercise",
 * "Light (1-3 days/week)", "Average (3-5 days/week)", "Active (6-7 days/week)").
 * Anything unrecognised falls back to the old app's default, average.
 */
internal fun parseActivityLevel(raw: String?): ActivityLevel {
    val value = raw?.trim()?.lowercase().orEmpty()
    return when {
        value.startsWith("sedentary") || value.startsWith("little") -> ActivityLevel.SEDENTARY
        value.startsWith("light") -> ActivityLevel.LIGHT
        value.startsWith("active") -> ActivityLevel.ACTIVE
        else -> ActivityLevel.AVERAGE
    }
}

/** Reads "cut", "bulk", "maintenance" in any case. Anything else falls back to maintenance. */
internal fun parseGoal(raw: String?): Goal = when (raw?.trim()?.lowercase()) {
    "cut" -> Goal.CUT
    "bulk" -> Goal.BULK
    else -> Goal.MAINTENANCE
}
