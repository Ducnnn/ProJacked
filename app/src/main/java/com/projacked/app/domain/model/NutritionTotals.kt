package com.projacked.app.domain.model

/** Summed nutrition values: calories in kcal, the rest in grams. */
data class NutritionTotals(
    val calories: Int = 0,
    val proteins: Int = 0,
    val fats: Int = 0,
    val carbs: Int = 0,
) {
    operator fun plus(other: NutritionTotals) = NutritionTotals(
        calories = calories + other.calories,
        proteins = proteins + other.proteins,
        fats = fats + other.fats,
        carbs = carbs + other.carbs,
    )
}
