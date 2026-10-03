package com.projacked.app.domain.repository

import com.projacked.app.domain.model.Meal
import com.projacked.app.domain.model.MealDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Meals logged per date (`MealDays`). */
interface NutritionRepository {

    /** Live meals for [date]. An empty day when nothing is logged or nobody is signed in. */
    fun observeMealDay(date: LocalDate): Flow<MealDay>

    /** Adds [meal] to [date], creating the day's document if needed. */
    suspend fun addMeal(date: LocalDate, meal: Meal): Result<Unit>
}
