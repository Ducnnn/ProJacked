package com.projacked.app.domain.model

import java.util.UUID

/**
 * A named meal made of products.
 *
 * @property id unique per meal, so two identical meals on the same day are kept as two entries.
 */
data class Meal(
    val id: String,
    val name: String,
    val products: List<Product>,
) {
    val totals: NutritionTotals get() = products.fold(NutritionTotals()) { sum, product -> sum + product.totals }

    companion object {
        fun create(name: String, products: List<Product>) =
            Meal(id = UUID.randomUUID().toString(), name = name, products = products)
    }
}
