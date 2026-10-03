package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.ActivityLevel
import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.Gender
import com.projacked.app.domain.model.Goal
import com.projacked.app.domain.model.MacroGoals
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Recommended daily calories and macros, using the old app's formula:
 * Mifflin-St Jeor BMR × activity multiplier × goal factor, protein and fat in g per kg of body weight,
 * and carbs filling the remaining calories. Results are rounded to one decimal place, half up, on the number as
 * it prints (232.95 → 233.0), which gives the same results as the old app's `String.format("%.1f")`.
 *
 * Fixes from the old app: inputs are enums, so gender and activity always match (the old string checks
 * never matched "Male" or "Little-No Exercise"), and rounding doesn't depend on the device locale.
 */
class CalculateMacros @Inject constructor() {

    operator fun invoke(parameters: BodyParameters): MacroGoals = with(parameters) {
        val genderOffset = when (gender) {
            Gender.MALE -> 5.0
            Gender.FEMALE -> -161.0
        }
        val bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + genderOffset

        val activityMultiplier = when (activity) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.4
            ActivityLevel.AVERAGE -> 1.5
            ActivityLevel.ACTIVE -> 1.7
        }
        val goalFactor = when (goal) {
            Goal.CUT -> 0.8
            Goal.BULK -> 1.2
            Goal.MAINTENANCE -> 1.0
        }
        val calories = bmr * activityMultiplier * goalFactor

        val proteinPerKg = when (goal) {
            Goal.CUT -> 2.4
            Goal.BULK -> 1.8
            Goal.MAINTENANCE -> 1.2
        }
        val fatPerKg = when (goal) {
            Goal.CUT -> 0.5
            Goal.BULK -> 1.0
            Goal.MAINTENANCE -> 0.8
        }
        val protein = proteinPerKg * weightKg
        val fats = fatPerKg * weightKg
        val carbs = (calories - (protein * PROTEIN_KCAL_PER_GRAM + fats * FAT_KCAL_PER_GRAM)) / CARB_KCAL_PER_GRAM

        MacroGoals(
            protein = protein.roundToOneDecimal(),
            fats = fats.roundToOneDecimal(),
            carbs = carbs.roundToOneDecimal(),
            calories = calories.roundToOneDecimal(),
        )
    }

    // BigDecimal.valueOf rounds the shortest decimal form of the double ("232.95"). BigDecimal(this) would round
    // its exact binary value (232.9499…) and give 232.9.
    private fun Double.roundToOneDecimal(): Double =
        BigDecimal.valueOf(this).setScale(1, RoundingMode.HALF_UP).toDouble()

    private companion object {
        const val PROTEIN_KCAL_PER_GRAM = 4
        const val FAT_KCAL_PER_GRAM = 9
        const val CARB_KCAL_PER_GRAM = 4
    }
}
