package com.projacked.app.ui.screens.currentday

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.SetInputError
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.domain.usecase.ParseReps
import com.projacked.app.domain.usecase.ParseWeight
import com.projacked.app.fakes.FakeTrainingRepository
import com.projacked.app.fakes.MainDispatcherRule
import com.projacked.app.fakes.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentDayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val today = LocalDate.of(2026, 10, 12)
    private val clock = TestClock(today)
    private val training = FakeTrainingRepository()

    private val bench = Exercise(
        name = "Bench press",
        muscleGroup = MuscleGroup.CHEST,
        notes = "Pause",
        sets = listOf(WorkoutSet(10, 60.0), WorkoutSet(8, 62.5), WorkoutSet()),
    )
    private val overhead = Exercise("Overhead press", MuscleGroup.SHOULDERS, sets = listOf(WorkoutSet()))
    private val pushUp = Exercise("Push-up", MuscleGroup.CHEST, sets = listOf(WorkoutSet(15, 0.0), WorkoutSet()))
    private val push = TrainingDay("Push", "#e53935", listOf(bench, overhead, pushUp))

    private lateinit var vm: CurrentDayViewModel
    private val state get() = vm.uiState.value
    private fun card(i: Int) = state.cards[i]
    private val saves get() = training.exerciseUpdates

    private fun TestScope.open(day: TrainingDay? = push) {
        if (day != null) training.days[today] = day
        vm = CurrentDayViewModel(training, clock, ParseReps(), ParseWeight())
        runCurrent()
    }

    @Test
    fun `loads the workout of the clock's date with every card on its last set`() = runTest {
        training.days[today] = push
        vm = CurrentDayViewModel(training, clock, ParseReps(), ParseWeight())
        assertEquals(CurrentDayLoad.Loading, state.load)
        runCurrent()
        assertEquals(CurrentDayLoad.Loaded("Push"), state.load)
        assertEquals(listOf("Bench press", "Overhead press", "Push-up"), state.cards.map { it.exercise.name })
        assertEquals(2, card(0).selectedSet)
        assertEquals("", card(0).repsText)
        assertEquals("", card(0).weightText)
        assertEquals(1, card(2).selectedSet)

        vm.onSelectSet(0, 1)
        assertEquals("8", card(0).repsText)
        assertEquals("62.5", card(0).weightText)
        vm.onSelectSet(0, 0)
        assertEquals("60", card(0).weightText)
    }

    @Test
    fun `a load failure gives the error state and retry loads again`() = runTest {
        training.days[today] = push
        training.getDayError = RuntimeException("offline")
        open(null)
        assertEquals(CurrentDayLoad.Error, state.load)
        training.getDayError = null
        vm.retry()
        runCurrent()
        assertEquals(CurrentDayLoad.Loaded("Push"), state.load)
        vm.retry() // only works from the error state
        assertEquals(CurrentDayLoad.Loaded("Push"), state.load)
    }

    @Test
    fun `no document, a rest day and a workout without exercises are the rest state and never save`() = runTest {
        listOf(null, TrainingDay.REST, TrainingDay("Cardio", "#1E88E5")).forEach { day ->
            training.days.clear()
            open(day)
            assertEquals(CurrentDayLoad.Rest, state.load)
            vm.onRepsChange(0, "5")
            vm.onAddSet(0)
            vm.onFinishedChange(0, true)
            vm.retrySave()
            advanceUntilIdle()
            assertTrue(saves.isEmpty())
        }
    }

    @Test
    fun `typing changes the model at once and saves the whole list after one second`() = runTest {
        open()
        vm.onRepsChange(0, "12")
        assertEquals(12, card(0).exercise.sets[2].reps)
        advanceTimeBy(999)
        assertTrue(saves.isEmpty())
        advanceTimeBy(2)
        assertEquals(1, saves.size)
        assertEquals(today, saves[0].first)
        assertEquals(state.cards.map { it.exercise }, saves[0].second)
    }

    @Test
    fun `three quick edits give one save with the last values`() = runTest {
        open()
        vm.onRepsChange(0, "1")
        advanceTimeBy(500)
        vm.onRepsChange(0, "12")
        advanceTimeBy(500)
        vm.onWeightChange(0, "70")
        advanceTimeBy(1_100)
        assertEquals(1, saves.size)
        assertEquals(WorkoutSet(12, 70.0), saves[0].second[0].sets[2])
    }

    @Test
    fun `typing on one card never changes another`() = runTest {
        open()
        vm.onRepsChange(0, "12")
        vm.onWeightChange(2, "5")
        assertEquals(overhead, card(1).exercise)
        assertEquals(bench.sets.dropLast(1), card(0).exercise.sets.dropLast(1))
        assertEquals(WorkoutSet(0, 5.0), card(2).exercise.sets[1])
    }

    @Test
    fun `invalid text sets the error and saves nothing, then valid text clears it`() = runTest {
        open()
        vm.onRepsChange(0, "abc")
        assertEquals(SetInputError.NOT_WHOLE_NUMBER, card(0).repsError)
        assertEquals("abc", card(0).repsText)
        assertEquals(bench, card(0).exercise)
        advanceTimeBy(2_000)
        assertTrue(saves.isEmpty())

        vm.onRepsChange(0, "8")
        assertNull(card(0).repsError)
        advanceTimeBy(1_001)
        assertEquals(1, saves.size)

        vm.onRepsChange(0, "")
        assertEquals(0, card(0).exercise.sets[2].reps)
    }

    @Test
    fun `weight accepts a comma and refuses too many decimals`() = runTest {
        open()
        vm.onWeightChange(0, "22,5")
        assertEquals(22.5, card(0).exercise.sets[2].weightKg, 0.0)
        vm.onWeightChange(0, "22.555")
        assertEquals(SetInputError.TOO_MANY_DECIMALS, card(0).weightError)
        assertEquals(22.5, card(0).exercise.sets[2].weightKg, 0.0)
    }

    @Test
    fun `plus is disabled on an empty last set, on another set, with an error and when finished`() = runTest {
        open()
        assertFalse(card(0).canAddSet) // empty last set
        vm.onAddSet(0)
        assertEquals(3, card(0).exercise.sets.size)

        vm.onRepsChange(0, "5")
        assertTrue(card(0).canAddSet)
        vm.onRepsChange(0, "x")
        assertFalse(card(0).canAddSet) // error
        vm.onRepsChange(0, "5")
        vm.onSelectSet(0, 0)
        assertFalse(card(0).canAddSet) // not the last set
        vm.onSelectSet(0, 2)
        vm.onFinishedChange(0, true)
        assertFalse(card(0).canAddSet)
    }

    @Test
    fun `plus appends a selected empty set and saves once, at once`() = runTest {
        open()
        vm.onRepsChange(0, "12")
        vm.onWeightChange(0, "70")
        vm.onAddSet(0)
        runCurrent()
        assertEquals(4, card(0).exercise.sets.size)
        assertEquals(3, card(0).selectedSet)
        assertEquals("", card(0).repsText)
        assertEquals("", card(0).weightText)
        assertEquals(1, saves.size)
        assertEquals(WorkoutSet(12, 70.0), saves[0].second[0].sets[2])
        assertEquals(WorkoutSet(), saves[0].second[0].sets[3])
        advanceTimeBy(2_000)
        assertEquals(1, saves.size) // the typing timer was cancelled
    }

    @Test
    fun `plus allows a bodyweight set, reps without weight`() = runTest {
        open()
        assertFalse(card(2).canAddSet) // 0x0
        vm.onRepsChange(2, "15")
        assertTrue(card(2).canAddSet)
    }

    @Test
    fun `delete removes the last set and selects the new last when it was selected`() = runTest {
        open()
        assertTrue(card(0).canDeleteSet)
        vm.onDeleteSet(0)
        runCurrent()
        assertEquals(2, card(0).exercise.sets.size)
        assertEquals(1, card(0).selectedSet)
        assertEquals("8", card(0).repsText)
        assertEquals(1, saves.size)
    }

    @Test
    fun `delete with another set selected keeps the selection`() = runTest {
        open()
        vm.onSelectSet(0, 0)
        vm.onDeleteSet(0)
        assertEquals(0, card(0).selectedSet)
        assertEquals("10", card(0).repsText)
        assertEquals(2, card(0).exercise.sets.size)
    }

    @Test
    fun `delete clears the only set, and is disabled on a single empty set`() = runTest {
        open(TrainingDay("Push", "#e53935", listOf(Exercise("A", sets = listOf(WorkoutSet(5, 20.0))))))
        vm.onDeleteSet(0)
        runCurrent()
        assertEquals(listOf(WorkoutSet()), card(0).exercise.sets)
        assertEquals("", card(0).repsText)
        assertFalse(card(0).canDeleteSet)
        vm.onDeleteSet(0)
        runCurrent()
        assertEquals(1, saves.size)
    }

    @Test
    fun `finished sets completed, saves at once, drops invalid text and locks edits`() = runTest {
        open()
        vm.onRepsChange(0, "abc")
        vm.onFinishedChange(0, true)
        runCurrent()
        assertTrue(card(0).exercise.completed)
        assertEquals("", card(0).repsText)
        assertNull(card(0).repsError)
        assertEquals(1, saves.size)
        assertTrue(saves[0].second[0].completed)

        vm.onRepsChange(0, "5")
        vm.onWeightChange(0, "5")
        vm.onDeleteSet(0)
        advanceTimeBy(2_000)
        assertEquals(bench.withCompleted(true), card(0).exercise)
        assertEquals(1, saves.size)

        vm.onSelectSet(0, 1)
        assertEquals("8", card(0).repsText)

        vm.onFinishedChange(0, false)
        runCurrent()
        assertTrue(card(0).inputsEnabled)
        assertEquals(2, saves.size)
        assertFalse(saves[1].second[0].completed)
    }

    @Test
    fun `flush saves a pending edit at once, once`() = runTest {
        open()
        vm.flushPendingSave()
        runCurrent()
        assertTrue(saves.isEmpty())

        vm.onRepsChange(0, "12")
        vm.flushPendingSave()
        runCurrent()
        assertEquals(1, saves.size)
        vm.flushPendingSave()
        advanceTimeBy(2_000)
        assertEquals(1, saves.size)
    }

    @Test
    fun `a failing save shows a message and retry saves again`() = runTest {
        open()
        training.updateExercisesError = RuntimeException("denied")
        vm.onFinishedChange(0, true)
        runCurrent()
        assertEquals(CurrentDayMessage.SAVE_FAILED, state.message)
        vm.onMessageShown()
        assertNull(state.message)

        vm.retrySave()
        runCurrent()
        assertEquals(2, saves.size)
        assertEquals(CurrentDayMessage.SAVE_FAILED, state.message)

        training.updateExercisesError = null
        vm.onMessageShown()
        vm.retrySave()
        runCurrent()
        assertNull(state.message)
    }

    @Test
    fun `saves go to the date the screen opened with`() = runTest {
        open()
        clock.date = today.plusDays(1)
        vm.onFinishedChange(0, true)
        runCurrent()
        assertEquals(today, saves.single().first)
    }

    @Test
    fun `only exercises are ever written`() = runTest {
        open()
        vm.onRepsChange(0, "3")
        vm.onAddSet(0)
        vm.onDeleteSet(0)
        vm.onFinishedChange(1, true)
        advanceUntilIdle()
        assertEquals(saves.size, training.writeCount)
        assertTrue(training.savedBatches.isEmpty())
        assertTrue(training.savedTemplates.isEmpty())
    }
}
