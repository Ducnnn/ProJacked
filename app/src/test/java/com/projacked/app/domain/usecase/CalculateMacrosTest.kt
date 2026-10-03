package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.ActivityLevel
import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.Gender
import com.projacked.app.domain.model.Goal
import com.projacked.app.domain.model.MacroGoals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CalculateMacrosTest {

    private val calculateMacros = CalculateMacros()

    private val base = BodyParameters(
        gender = Gender.MALE,
        heightCm = 180,
        weightKg = 80.0,
        age = 25,
        activity = ActivityLevel.AVERAGE,
        goal = Goal.MAINTENANCE,
    )

    @Test
    fun `male maintenance with average activity`() {
        // BMR = 10*80 + 6.25*180 - 5*25 + 5 = 1805; × 1.5 = 2707.5 kcal
        // protein 1.2*80 = 96, fat 0.8*80 = 64, carbs (2707.5 - 384 - 576) / 4 = 436.875 → 436.9
        assertEquals(
            MacroGoals(protein = 96.0, fats = 64.0, carbs = 436.9, calories = 2707.5),
            calculateMacros(base),
        )
    }

    @Test
    fun `female uses the -161 offset`() {
        // This never happened in the old app: its check compared "male" with the spinner's "Male".
        val result = calculateMacros(base.copy(gender = Gender.FEMALE))
        // BMR = 1805 - 166 = 1639; × 1.5 = 2458.5
        assertEquals(2458.5, result.calories, 0.0)
    }

    @Test
    fun `sedentary uses 1_2, the multiplier the old app never applied`() {
        val result = calculateMacros(base.copy(activity = ActivityLevel.SEDENTARY))
        assertEquals(2166.0, result.calories, 0.0) // 1805 × 1.2
    }

    @Test
    fun `each activity level has its own multiplier`() {
        val calories = ActivityLevel.entries.associateWith { calculateMacros(base.copy(activity = it)).calories }
        assertEquals(2166.0, calories.getValue(ActivityLevel.SEDENTARY), 0.0)
        assertEquals(2527.0, calories.getValue(ActivityLevel.LIGHT), 0.0)
        assertEquals(2707.5, calories.getValue(ActivityLevel.AVERAGE), 0.0)
        assertEquals(3068.5, calories.getValue(ActivityLevel.ACTIVE), 0.0)
    }

    @Test
    fun `cut lowers calories by 20 percent with high protein and low fat`() {
        val result = calculateMacros(base.copy(goal = Goal.CUT))
        assertEquals(2166.0, result.calories, 0.0) // 2707.5 × 0.8
        assertEquals(192.0, result.protein, 0.0)   // 2.4 g/kg
        assertEquals(40.0, result.fats, 0.0)       // 0.5 g/kg
        assertEquals(259.5, result.carbs, 0.0)     // (2166 - 768 - 360) / 4
    }

    @Test
    fun `bulk raises calories by 20 percent`() {
        val result = calculateMacros(base.copy(goal = Goal.BULK))
        assertEquals(3249.0, result.calories, 0.0) // 2707.5 × 1.2
        assertEquals(144.0, result.protein, 0.0)   // 1.8 g/kg
        assertEquals(80.0, result.fats, 0.0)       // 1.0 g/kg
        assertEquals(488.3, result.carbs, 0.0)     // (3249 - 576 - 720) / 4 = 488.25, rounded half up
    }

    @Test
    fun `fractional body weight is supported`() {
        val result = calculateMacros(base.copy(weightKg = 80.5))
        assertEquals(96.6, result.protein, 0.0) // 1.2 × 80.5
    }

    @Test
    fun `halfway values round up like the old app`() {
        // BMR = 10*60 + 6.25*170 - 5*25 - 161 = 1376.5; × 1.2 = 1651.8 kcal
        // protein 1.2*60 = 72, fat 0.8*60 = 48, carbs (1651.8 - 288 - 432) / 4 = 232.95 → 233.0
        // (rounding the double's exact binary value, 232.9499…, would give 232.9)
        val result = calculateMacros(
            BodyParameters(
                gender = Gender.FEMALE,
                heightCm = 170,
                weightKg = 60.0,
                age = 25,
                activity = ActivityLevel.SEDENTARY,
                goal = Goal.MAINTENANCE,
            ),
        )
        assertEquals(233.0, result.carbs, 0.0)
        assertEquals(1651.8, result.calories, 0.0)
    }

    @Test
    fun `rounding does not depend on the device locale`() {
        // The old app crashed here: String.format gave "436,9" on decimal-comma locales and toDouble() threw.
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals(436.9, calculateMacros(base).carbs, 0.0)
        } finally {
            Locale.setDefault(original)
        }
    }
}
