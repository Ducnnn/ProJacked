package com.projacked.app.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * `users/{uid}` as stored in Firestore. Field names match the old app's documents.
 *
 * Stored text values: the old app wrote `gender` as "male" or spinner text like "Male", and `activity` as
 * "average" or spinner text like "Light (1-3 days/week)". ProJacked writes stable lowercase keys and reads every
 * old variant (see ProfileMappers).
 */
data class UserDto(
    var email: String = "",
    var displayName: String = "",
    @ServerTimestamp var createdAt: Timestamp? = null,
    var gender: String = "",
    var height: Int = 0,
    var weight: Double = 0.0,
    var age: Int = 0,
    var activity: String = "",
    var goal: String = "",
    var macroGoals: MacroGoalsDto? = null,
)

data class MacroGoalsDto(
    var protein: Double = 0.0,
    var fats: Double = 0.0,
    var carbs: Double = 0.0,
    var calories: Double = 0.0,
)
