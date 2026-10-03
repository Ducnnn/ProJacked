package com.projacked.app.data.mapper

import com.projacked.app.data.remote.dto.MealDayDto
import com.projacked.app.data.remote.dto.MealDto
import com.projacked.app.data.remote.dto.ProductDto
import com.projacked.app.domain.model.Meal
import com.projacked.app.domain.model.MealDay
import com.projacked.app.domain.model.Product
import java.time.LocalDate

internal fun MealDayDto.toDomain(date: LocalDate) = MealDay(date = date, meals = meals.map { it.toDomain() })

internal fun MealDto.toDomain() = Meal(id = id, name = name, products = products.map { it.toDomain() })

internal fun Meal.toDto() = MealDto(id = id, name = name, products = products.map { it.toDto() })

internal fun ProductDto.toDomain() =
    Product(name = name, calories = calories, proteins = proteins, fats = fats, carbs = carbs)

internal fun Product.toDto() =
    ProductDto(name = name, calories = calories, proteins = proteins, fats = fats, carbs = carbs)
