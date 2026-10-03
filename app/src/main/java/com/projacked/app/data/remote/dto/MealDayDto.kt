package com.projacked.app.data.remote.dto

/**
 * `MealDays/{yyyy-MM-dd}`: one document per date holding that day's meals. The old app never managed to write
 * these documents, so this shape is new (it follows the old app's intent, plus a per-meal `id`).
 */
data class MealDayDto(
    var meals: List<MealDto> = emptyList(),
)

data class MealDto(
    var id: String = "",
    var name: String = "",
    var products: List<ProductDto> = emptyList(),
)

data class ProductDto(
    var name: String = "",
    var calories: Int = 0,
    var proteins: Int = 0,
    var fats: Int = 0,
    var carbs: Int = 0,
)
