package com.projacked.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseTest {

    private val bench = Exercise(name = "Bench press", sets = listOf(WorkoutSet(10, 60.0), WorkoutSet()))

    @Test
    fun `a new exercise starts with one empty set`() {
        assertEquals(listOf(WorkoutSet()), Exercise(name = "Squat").sets)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an exercise can't have zero sets`() {
        Exercise(name = "Squat", sets = emptyList())
    }

    @Test
    fun `plus on the last set saves it and appends an empty set`() {
        val result = bench.commitSetAndAddNext(index = 1, set = WorkoutSet(8, 62.5))
        assertEquals(listOf(WorkoutSet(10, 60.0), WorkoutSet(8, 62.5), WorkoutSet()), result.sets)
    }

    @Test
    fun `plus does nothing when the selected set isn't the last one`() {
        assertSame(bench, bench.commitSetAndAddNext(index = 0, set = WorkoutSet(12, 60.0)))
    }

    @Test
    fun `plus does nothing when the values are empty`() {
        assertSame(bench, bench.commitSetAndAddNext(index = 1, set = WorkoutSet(0, 0.0)))
    }

    @Test
    fun `plus accepts weight without reps and reps without weight`() {
        assertEquals(3, bench.commitSetAndAddNext(1, WorkoutSet(0, 20.0)).sets.size)
        assertEquals(3, bench.commitSetAndAddNext(1, WorkoutSet(15, 0.0)).sets.size)
    }

    @Test
    fun `delete removes the last set, not the selected one`() {
        assertEquals(listOf(WorkoutSet(10, 60.0)), bench.removeLastSet().sets)
    }

    @Test
    fun `delete on the only set clears it instead`() {
        val single = Exercise(name = "Squat", sets = listOf(WorkoutSet(5, 100.0)))
        assertEquals(listOf(WorkoutSet()), single.removeLastSet().sets)
    }

    @Test
    fun `updateSet replaces one set`() {
        assertEquals(WorkoutSet(9, 61.0), bench.updateSet(0, WorkoutSet(9, 61.0)).sets[0])
    }

    @Test
    fun `resetProgress clears values and completion but keeps the set count`() {
        val done = bench.withCompleted(true).resetProgress()
        assertEquals(listOf(WorkoutSet(), WorkoutSet()), done.sets)
        assertFalse(done.completed)
    }
}

class TrainingDayTest {

    @Test
    fun `rest day is recognised, with any colour case`() {
        assertTrue(TrainingDay.REST.isRest)
        assertTrue(TrainingDay("Rest", "#FFA9A3").isRest)
    }

    @Test
    fun `a day named Rest with exercises or another colour is a real workout`() {
        assertFalse(TrainingDay("Rest", "#ffa9a3", listOf(Exercise("Stretch"))).isRest)
        assertFalse(TrainingDay("Rest", "#00ff00").isRest)
    }
}

class NutritionTotalsTest {

    private val chicken = Product("Chicken", calories = 237, proteins = 23, fats = 8, carbs = 0)
    private val bread = Product("Bread", calories = 200, proteins = 4, fats = 17, carbs = 20)

    @Test
    fun `meal totals add up every macro separately`() {
        // The old Meals screen added proteins into the fats total.
        assertEquals(NutritionTotals(calories = 437, proteins = 27, fats = 25, carbs = 20), Meal("1", "Lunch", listOf(chicken, bread)).totals)
    }

    @Test
    fun `day totals add up all meals`() {
        val day = MealDay(
            date = java.time.LocalDate.of(2026, 10, 2),
            meals = listOf(Meal("1", "Lunch", listOf(chicken)), Meal("2", "Snack", listOf(bread))),
        )
        assertEquals(NutritionTotals(437, 27, 25, 20), day.totals)
    }

    @Test
    fun `identical meals created separately get different ids`() {
        val a = Meal.create("Banana", emptyList())
        val b = Meal.create("Banana", emptyList())
        assertFalse(a.id == b.id)
    }
}
