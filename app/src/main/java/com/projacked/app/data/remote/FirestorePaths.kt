package com.projacked.app.data.remote

import java.time.LocalDate

/**
 * Every Firestore path the app uses, in one place. These match the old app's paths exactly: don't change them
 * without an approved data-migration plan (existing users' data lives here).
 */
internal object FirestorePaths {
    private const val USERS = "users"
    private const val CUSTOM_DAYS = "CustomDays"
    private const val TRAINING_DAYS = "TrainingDays"
    private const val MEAL_DAYS = "MealDays"

    fun user(uid: String) = "$USERS/$uid"

    fun templates(uid: String) = "${user(uid)}/$CUSTOM_DAYS"
    fun template(uid: String, name: String) = "${templates(uid)}/$name"

    fun trainingDays(uid: String) = "${user(uid)}/$TRAINING_DAYS"
    fun trainingDay(uid: String, date: LocalDate) = "${trainingDays(uid)}/${dateId(date)}"

    fun mealDay(uid: String, date: LocalDate) = "${user(uid)}/$MEAL_DAYS/${dateId(date)}"

    /** Date documents are keyed by ISO date, e.g. "2026-10-02" (same as the old app). */
    fun dateId(date: LocalDate): String = date.toString()
}
