package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.fakes.FakeTrainingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrainingUseCasesTest {

    private val repository = FakeTrainingRepository()
    private val assignTemplateToDate = AssignTemplateToDate(repository)
    private val copyPreviousWeek = CopyPreviousWeek(repository, assignTemplateToDate)

    private val monday = LocalDate.of(2026, 9, 28)

    private val push = TrainingDay(
        name = "Push",
        colorHex = "#ff8800",
        exercises = listOf(
            Exercise(
                name = "Bench press",
                sets = listOf(WorkoutSet(10, 60.0), WorkoutSet(8, 62.5)),
                completed = true,
            ),
        ),
    )

    @Test
    fun `assigning a template stores a copy on that date`() = runTest {
        assertTrue(assignTemplateToDate(monday, push).isSuccess)
        assertEquals(push, repository.days[monday])
    }

    @Test
    fun `assigning rest deletes the stored workout`() = runTest {
        repository.days[monday] = push
        assertTrue(assignTemplateToDate(monday, TrainingDay.REST).isSuccess)
        assertFalse(monday in repository.days)
    }

    @Test
    fun `copy previous week resets progress but keeps the number of sets`() = runTest {
        repository.days[monday.minusWeeks(1)] = push

        assertTrue(copyPreviousWeek(monday).isSuccess)

        val copied = repository.days.getValue(monday)
        assertEquals("Push", copied.name)
        assertEquals(listOf(WorkoutSet(), WorkoutSet()), copied.exercises.single().sets)
        assertFalse(copied.exercises.single().completed)
    }

    @Test
    fun `copy previous week turns days that were rest into rest`() = runTest {
        val wednesday = monday.plusDays(2)
        repository.days[wednesday] = push // something planned this week, but last Wednesday was rest

        assertTrue(copyPreviousWeek(monday).isSuccess)

        assertFalse(wednesday in repository.days)
    }

    @Test
    fun `copy previous week only touches the 7 days of the week`() = runTest {
        val nextMonday = monday.plusWeeks(1)
        repository.days[nextMonday] = push

        copyPreviousWeek(monday)

        assertEquals(push, repository.days[nextMonday])
    }

    @Test
    fun `copy previous week stops and reports the first failure`() = runTest {
        repository.failingDate = monday.plusDays(3).minusWeeks(1)

        val result = copyPreviousWeek(monday)

        assertTrue(result.isFailure)
    }
}
