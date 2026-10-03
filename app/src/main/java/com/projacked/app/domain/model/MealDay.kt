package com.projacked.app.domain.model

import java.time.LocalDate

/** Everything eaten on one date. */
data class MealDay(
    val date: LocalDate,
    val meals: List<Meal> = emptyList(),
) {
    val totals: NutritionTotals get() = meals.fold(NutritionTotals()) { sum, meal -> sum + meal.totals }
}
