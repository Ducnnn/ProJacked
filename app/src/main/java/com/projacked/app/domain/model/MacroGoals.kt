package com.projacked.app.domain.model

/** Daily nutrition targets, in grams (protein, fats, carbs) and kcal (calories). */
data class MacroGoals(
    val protein: Double,
    val fats: Double,
    val carbs: Double,
    val calories: Double,
) {
    companion object {
        /** The targets the old app wrote for every new account. */
        val NEW_USER_DEFAULT = MacroGoals(protein = 150.0, fats = 100.0, carbs = 200.0, calories = 1500.0)
    }
}
