package com.projacked.app.data.mapper

import com.projacked.app.data.remote.dto.ExerciseDto
import com.projacked.app.data.remote.dto.MacroGoalsDto
import com.projacked.app.data.remote.dto.MealDayDto
import com.projacked.app.data.remote.dto.MealDto
import com.projacked.app.data.remote.dto.ProductDto
import com.projacked.app.data.remote.dto.TrainingDayDto
import com.projacked.app.data.remote.dto.UserDto
import com.projacked.app.data.remote.dto.WorkoutSetDto
import com.projacked.app.domain.model.ActivityLevel
import com.projacked.app.domain.model.BodyParameters
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.Gender
import com.projacked.app.domain.model.Goal
import com.projacked.app.domain.model.MacroGoals
import com.projacked.app.domain.model.Meal
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.Product
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProfileMappersTest {

    @Test
    fun `reads a profile exactly as the old app's sign-up wrote it`() {
        val legacy = UserDto(
            email = "old@example.com",
            displayName = "Old User",
            gender = "male",
            height = 180,
            weight = 80.0,
            age = 18,
            activity = "average",
            goal = "maintenance",
            macroGoals = MacroGoalsDto(protein = 150.0, fats = 100.0, carbs = 200.0, calories = 1500.0),
        )

        assertEquals(UserProfile.newUser("old@example.com", "Old User"), legacy.toDomain())
    }

    @Test
    fun `reads the old parameters screen's spinner text`() {
        assertEquals(Gender.MALE, parseGender("Male"))
        assertEquals(Gender.FEMALE, parseGender("Female"))
        assertEquals(ActivityLevel.SEDENTARY, parseActivityLevel("Little-No Exercise"))
        assertEquals(ActivityLevel.LIGHT, parseActivityLevel("Light (1-3 days/week)"))
        assertEquals(ActivityLevel.AVERAGE, parseActivityLevel("Average (3-5 days/week)"))
        assertEquals(ActivityLevel.ACTIVE, parseActivityLevel("Active (6-7 days/week)"))
        assertEquals(Goal.BULK, parseGoal("bulk"))
        assertEquals(Goal.CUT, parseGoal("cut"))
        assertEquals(Goal.MAINTENANCE, parseGoal("maintenance"))
    }

    @Test
    fun `unknown or missing values fall back to the old defaults`() {
        assertEquals(Gender.MALE, parseGender(null))
        assertEquals(ActivityLevel.AVERAGE, parseActivityLevel(""))
        assertEquals(Goal.MAINTENANCE, parseGoal("whatever"))
    }

    @Test
    fun `missing macro goals fall back to the new-user default`() {
        assertEquals(MacroGoals.NEW_USER_DEFAULT, UserDto().toDomain().macroGoals)
    }

    @Test
    fun `writes stable lowercase keys that it can read back`() {
        Gender.entries.forEach { assertEquals(it, parseGender(it.storageKey)) }
        ActivityLevel.entries.forEach { assertEquals(it, parseActivityLevel(it.storageKey)) }
        Goal.entries.forEach { assertEquals(it, parseGoal(it.storageKey)) }
    }

    @Test
    fun `profile round-trips and leaves createdAt for the server`() {
        val profile = UserProfile(
            email = "a@b.com",
            displayName = "A",
            parameters = BodyParameters(Gender.FEMALE, 165, 58.5, 30, ActivityLevel.LIGHT, Goal.CUT),
            macroGoals = MacroGoals(120.0, 50.0, 180.0, 1700.0),
        )
        val dto = profile.toDto()
        assertNull(dto.createdAt)
        assertEquals(profile, dto.toDomain())
    }

    @Test
    fun `parameter update touches only the six parameter fields`() {
        val fields = BodyParameters(Gender.FEMALE, 165, 58.5, 30, ActivityLevel.LIGHT, Goal.CUT).toFieldMap()
        assertEquals(
            mapOf("gender" to "female", "height" to 165, "weight" to 58.5, "age" to 30, "activity" to "light", "goal" to "cut"),
            fields,
        )
    }
}

class TrainingMappersTest {

    @Test
    fun `reads a workout as the old app stored it`() {
        val legacy = TrainingDayDto(
            name = "Push",
            color = "#ff8800",
            exercises = listOf(
                ExerciseDto(
                    name = "Bench press",
                    muscle = "Chest",
                    description = "Pause at the bottom",
                    sets = listOf(WorkoutSetDto(reps = 10, weight = 60.0)),
                    completed = true,
                ),
            ),
        )

        assertEquals(
            TrainingDay(
                name = "Push",
                colorHex = "#ff8800",
                exercises = listOf(
                    Exercise("Bench press", MuscleGroup.CHEST, "Pause at the bottom", listOf(WorkoutSet(10, 60.0)), true),
                ),
            ),
            legacy.toDomain(),
        )
    }

    @Test
    fun `missing name and colour become the rest day's values`() {
        assertEquals(TrainingDay.REST, TrainingDayDto().toDomain())
    }

    @Test
    fun `an exercise stored without sets gets one empty set`() {
        assertEquals(listOf(WorkoutSet()), ExerciseDto(name = "Plank").toDomain().sets)
    }

    @Test
    fun `muscle groups use the old spinner text and parse case-insensitively`() {
        assertEquals("Shoulders", MuscleGroup.SHOULDERS.storageName)
        assertEquals(MuscleGroup.LEGS, parseMuscleGroup("legs"))
        assertEquals(MuscleGroup.CHEST, parseMuscleGroup("Neck")) // unknown → old default
        MuscleGroup.entries.forEach { assertEquals(it, parseMuscleGroup(it.storageName)) }
    }

    @Test
    fun `fractional weights round-trip`() {
        val day = TrainingDay("Legs", "#00ff00", listOf(Exercise("Squat", MuscleGroup.LEGS, sets = listOf(WorkoutSet(5, 102.5)))))
        assertEquals(day, day.toDto().toDomain())
    }
}

class NutritionMappersTest {

    @Test
    fun `meal day round-trips`() {
        val date = LocalDate.of(2026, 10, 2)
        val meal = Meal("id-1", "Lunch", listOf(Product("Chicken", 237, 23, 8, 0)))
        val dto = MealDayDto(meals = listOf(meal.toDto()))

        assertEquals(listOf(meal), dto.toDomain(date).meals)
        assertEquals(date, dto.toDomain(date).date)
    }

    @Test
    fun `product fields map one to one`() {
        val dto = ProductDto(name = "Bread", calories = 200, proteins = 4, fats = 17, carbs = 20)
        assertEquals(Product("Bread", calories = 200, proteins = 4, fats = 17, carbs = 20), dto.toDomain())
        assertEquals(dto, dto.toDomain().toDto())
    }

    @Test
    fun `meal keeps its id`() {
        assertEquals("id-7", MealDto(id = "id-7", name = "Snack").toDomain().id)
    }
}
