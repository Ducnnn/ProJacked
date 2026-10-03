package com.projacked.app.domain.model

/** A food item with its values per serving (as entered by the user, like the old app). */
data class Product(
    val name: String,
    val calories: Int,
    val proteins: Int,
    val fats: Int,
    val carbs: Int,
) {
    val totals: NutritionTotals get() = NutritionTotals(calories, proteins, fats, carbs)
}
