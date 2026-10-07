package com.projacked.app.ui.screens.plan

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.domain.usecase.AssignTemplateToDate
import com.projacked.app.domain.usecase.CopyPreviousWeek
import com.projacked.app.fakes.FakeTrainingRepository
import com.projacked.app.fakes.MainDispatcherRule
import com.projacked.app.fakes.TestClock
import com.projacked.app.ui.screens.home.LoadStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingPlanViewModelTest {

    // Unconfined, so collectors and writes in viewModelScope run as soon as they are started.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val today = LocalDate.of(2026, 10, 6) // a Tuesday
    private val weekStart = LocalDate.of(2026, 10, 5)
    private val clock = TestClock(today)
    private val training = FakeTrainingRepository()

    private fun createViewModel() =
        TrainingPlanViewModel(training, AssignTemplateToDate(training), CopyPreviousWeek(training), clock)

    private lateinit var vm: TrainingPlanViewModel

    private val state get() = vm.uiState.value

    /** A view model that has been told the first visible week. */
    private fun start() {
        vm = createViewModel()
        vm.onVisibleWeekChanged(weekStart)
    }

    private val push = TrainingDay(
        "Push",
        "#e53935",
        listOf(Exercise("Bench press", sets = listOf(WorkoutSet(), WorkoutSet(), WorkoutSet()))),
    )

    private val logged = TrainingDay(
        "Legs",
        "#FFFFFF",
        listOf(Exercise("Squat", sets = listOf(WorkoutSet(5, 100.0)), completed = true)),
    )

    private fun templateNames() = (state.templates as TemplatesState.Loaded).templates.map { it.name }

    @Test
    fun `today and the strip range come from the clock`() {
        val s = createViewModel().uiState.value

        assertEquals(today, s.today)
        assertEquals(LocalDate.of(2018, 6, 1), s.startDate)
        assertEquals(LocalDate.of(2035, 2, 28), s.endDate)
    }

    @Test
    fun `nothing is requested before the first week is reported, then the window once`() {
        vm = createViewModel()
        assertTrue(training.observedRanges.isEmpty())

        vm.onVisibleWeekChanged(weekStart)

        assertEquals(listOf(weekStart.minusDays(7) to weekStart.plusDays(14)), training.observedRanges)
    }

    @Test
    fun `a loaded date with no stored workout is rest, and data is live`() {
        start()

        assertEquals(LoadStatus.LOADED, state.windowStatus)
        assertTrue(today in state.loadedDates)
        assertFalse(today in state.days)

        training.setDay(today, push)

        assertEquals(push, state.days[today])
    }

    @Test
    fun `a new visible week requests its window and loaded dates stay loaded`() {
        start()
        val next = weekStart.plusDays(7)

        vm.onVisibleWeekChanged(next)

        assertEquals(next.minusDays(7) to next.plusDays(14), training.observedRanges.last())
        assertTrue(weekStart.minusDays(3) in state.loadedDates)
        assertTrue(next.plusDays(13) in state.loadedDates)
    }

    @Test
    fun `an error sets the error status and retryWeek recovers`() {
        training.observeDaysError = IllegalStateException("boom")
        start()
        assertEquals(LoadStatus.ERROR, state.windowStatus)

        training.observeDaysError = null
        vm.retryWeek()

        assertEquals(LoadStatus.LOADED, state.windowStatus)
    }

    @Test
    fun `templates are sorted ignoring case and update live`() {
        listOf("Upper body", "Push", "pull", "Legs").forEach { training.setTemplate(TrainingDay(it, "#ff0000")) }
        start()

        assertEquals(listOf("Legs", "pull", "Push", "Upper body"), templateNames())

        training.setTemplate(TrainingDay("arms", "#00ff00"))

        assertEquals(listOf("arms", "Legs", "pull", "Push", "Upper body"), templateNames())
    }

    @Test
    fun `a template error shows, and retryTemplates recovers`() {
        training.observeTemplatesError = IllegalStateException("boom")
        start()
        assertEquals(TemplatesState.Error, state.templates)

        training.observeTemplatesError = null
        vm.retryTemplates()

        assertTrue(state.templates is TemplatesState.Loaded)
    }

    @Test
    fun `tapping a loaded date opens the assign dialog, an unloaded one does nothing`() {
        start()

        vm.onDayClick(today)
        assertEquals(PlanDialog.Assign(today), state.dialog)

        vm.dismissDialog()
        vm.onDayClick(today.plusDays(60))
        assertNull(state.dialog)
    }

    @Test
    fun `choosing a template on a date without progress saves an exact copy and closes the dialog`() {
        start()
        vm.onDayClick(today)

        vm.onChoose(push)

        assertNull(state.dialog)
        assertEquals(push, training.days[today])
    }

    @Test
    fun `choosing rest deletes, and on a date with no stored workout writes nothing`() {
        training.days[today] = push
        start()
        vm.onDayClick(today)
        vm.onChoose(TrainingDay.REST)
        assertFalse(today in training.days)

        val writes = training.writeCount
        vm.onDayClick(today)
        vm.onChoose(TrainingDay.REST)
        assertEquals(writes, training.writeCount)
    }

    @Test
    fun `choosing on a date with progress asks first, confirming writes and dismissing doesn't`() {
        training.days[today] = logged
        start()
        vm.onDayClick(today)

        vm.onChoose(push)

        assertEquals(PlanDialog.ConfirmReplace(today, push), state.dialog)
        assertEquals(logged, training.days[today])

        vm.dismissDialog()
        assertEquals(logged, training.days[today])

        vm.onDayClick(today)
        vm.onChoose(TrainingDay.REST)
        vm.confirmReplace()
        assertNull(state.dialog)
        assertFalse(today in training.days)
    }

    @Test
    fun `a failed save gives the save failed message, and onMessageShown clears it`() {
        start()
        training.writeError = IllegalStateException("rejected")
        vm.onDayClick(today)

        vm.onChoose(push)

        assertEquals(PlanMessage.SaveFailed, state.message)
        vm.onMessageShown()
        assertNull(state.message)
    }

    @Test
    fun `copy on an empty week runs straight away and ends with copied`() {
        training.days[weekStart.minusDays(7)] = push
        start()

        vm.onCopyClick()

        assertNull(state.dialog)
        assertEquals(PlanMessage.Copied, state.message)
        assertFalse(state.copyRunning)
        assertEquals(push.name, training.days[weekStart]?.name)
    }

    @Test
    fun `copy on a week with a workout asks first, and confirming runs it`() {
        training.days[today] = push
        training.days[weekStart.minusDays(7)] = push.copy(name = "Old")
        start()

        vm.onCopyClick()

        assertEquals(PlanDialog.ConfirmCopy(weekStart), state.dialog)
        assertTrue(training.savedBatches.isEmpty())

        vm.confirmCopy()

        assertNull(state.dialog)
        assertEquals("Old", training.days[weekStart]?.name)
        assertFalse(today in training.days)
    }

    @Test
    fun `copy does nothing while the week hasn't loaded`() {
        vm = createViewModel()

        vm.onCopyClick()

        assertTrue(training.serverReads.isEmpty())
    }

    @Test
    fun `copy running is true during the copy`() {
        start()
        val gate = CompletableDeferred<Unit>()
        training.serverReadGate = gate

        vm.onCopyClick()
        assertTrue(state.copyRunning)
        vm.onCopyClick() // ignored while running
        assertEquals(1, training.serverReads.size)

        gate.complete(Unit)
        assertFalse(state.copyRunning)
    }

    @Test
    fun `a failed server read ends with copy failed and no writes`() {
        training.serverReadError = IllegalStateException("offline")
        start()

        vm.onCopyClick()

        assertEquals(PlanMessage.CopyFailed, state.message)
        assertTrue(training.savedBatches.isEmpty())
        assertFalse(state.copyRunning)
    }

    @Test
    fun `long press then confirming deletes the template and leaves assigned dates alone`() {
        training.setTemplate(push)
        training.days[today] = push
        start()

        vm.onTemplateLongPress("Push")
        assertEquals(PlanDialog.ConfirmDelete("Push"), state.dialog)
        vm.confirmDelete()

        assertNull(state.dialog)
        assertTrue(training.templates.isEmpty())
        assertEquals(push, training.days[today])
    }

    @Test
    fun `a failed delete gives the delete failed message with the name`() {
        training.setTemplate(push)
        start()
        training.writeError = IllegalStateException("rejected")

        vm.onTemplateLongPress("Push")
        vm.confirmDelete()

        assertEquals(PlanMessage.DeleteFailed("Push"), state.message)
    }

    @Test
    fun `refreshToday after midnight moves today`() {
        start()

        clock.date = today.plusDays(1)
        vm.refreshToday()

        assertEquals(today.plusDays(1), state.today)
    }
}
