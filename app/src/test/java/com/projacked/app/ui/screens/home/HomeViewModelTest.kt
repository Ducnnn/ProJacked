package com.projacked.app.ui.screens.home

import com.projacked.app.domain.model.AttendanceLevel
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.UserProfile
import com.projacked.app.domain.usecase.ComputeAttendance
import com.projacked.app.fakes.FakeProfileRepository
import com.projacked.app.fakes.FakeTrainingRepository
import com.projacked.app.fakes.MainDispatcherRule
import com.projacked.app.fakes.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // Unconfined, so collectors in viewModelScope run as soon as they are started.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val today = LocalDate.of(2026, 10, 6)
    private val clock = TestClock(today)
    private val training = FakeTrainingRepository()
    private val profiles = FakeProfileRepository()

    private fun createViewModel() = HomeViewModel(training, profiles, ComputeAttendance(), clock)

    private fun workout(total: Int, finished: Int, name: String = "Push") = TrainingDay(
        name = name,
        colorHex = "#ff0000",
        exercises = List(total) { Exercise(name = "Exercise $it", completed = it < finished) },
    )

    private fun HomeViewModel.level(date: LocalDate) = uiState.value.levels[date]

    @Test
    fun `today and the calendar range come from the clock`() {
        val state = createViewModel().uiState.value

        assertEquals(today, state.today)
        assertEquals(YearMonth.of(2026, 10), state.visibleMonth)
        assertEquals(YearMonth.of(2018, 6), state.startMonth)
        assertEquals(YearMonth.of(2035, 2), state.endMonth)
    }

    @Test
    fun `the first window is the current month plus one on each side, requested once`() {
        createViewModel()

        val windows = training.observedRanges.filter { it.second.toEpochDay() - it.first.toEpochDay() > 1 }
        assertEquals(listOf(LocalDate.of(2026, 9, 1) to LocalDate.of(2026, 12, 1)), windows)
    }

    @Test
    fun `levels follow the attendance rules`() {
        training.days[today.minusDays(3)] = workout(4, 0) // past, nothing finished
        training.days[today] = workout(2, 0) // today, nothing finished
        training.days[today.plusDays(2)] = workout(2, 2) // future
        training.days[today.minusDays(5)] = TrainingDay(TrainingDay.REST_NAME, TrainingDay.REST_COLOR_HEX) // stored rest
        training.days[today.minusDays(6)] = workout(5, 4) // 80%
        training.days[today.minusDays(4)] = workout(4, 2) // 50%
        training.days[today.minusDays(2)] = workout(4, 1) // 25%
        training.days[today.minusDays(1)] = workout(3, 2) // 66%

        val vm = createViewModel()

        assertEquals(AttendanceLevel.MISSED, vm.level(today.minusDays(3)))
        assertEquals(AttendanceLevel.PLANNED, vm.level(today))
        assertEquals(AttendanceLevel.PLANNED, vm.level(today.plusDays(2)))
        assertEquals(AttendanceLevel.NOTHING_PLANNED, vm.level(today.minusDays(5)))
        assertEquals(AttendanceLevel.NOTHING_PLANNED, vm.level(today.plusDays(4))) // missing document
        assertEquals(AttendanceLevel.FULL, vm.level(today.minusDays(6)))
        assertEquals(AttendanceLevel.MEDIUM, vm.level(today.minusDays(4)))
        assertEquals(AttendanceLevel.LOW, vm.level(today.minusDays(2)))
        assertEquals(AttendanceLevel.HIGH, vm.level(today.minusDays(1)))
        assertEquals(LoadStatus.LOADED, vm.uiState.value.windowStatus)
    }

    @Test
    fun `a change in the data updates the levels live`() {
        training.days[today] = workout(2, 0)
        val vm = createViewModel()
        assertEquals(AttendanceLevel.PLANNED, vm.level(today))

        training.setDay(today, workout(2, 1))
        assertEquals(AttendanceLevel.MEDIUM, vm.level(today))

        training.removeDay(today)
        assertEquals(AttendanceLevel.NOTHING_PLANNED, vm.level(today))
    }

    @Test
    fun `months outside the first window have no level until they load`() {
        val vm = createViewModel()
        assertNull(vm.level(LocalDate.of(2026, 7, 15)))
    }

    @Test
    fun `changing the visible month requests the new window and keeps loaded months`() {
        training.days[LocalDate.of(2026, 9, 20)] = workout(4, 4)
        training.days[LocalDate.of(2026, 7, 15)] = workout(4, 4)
        val vm = createViewModel()

        vm.onVisibleMonthChanged(YearMonth.of(2026, 8))

        assertTrue(LocalDate.of(2026, 7, 1) to LocalDate.of(2026, 10, 1) in training.observedRanges)
        assertEquals(AttendanceLevel.FULL, vm.level(LocalDate.of(2026, 7, 15)))
        assertEquals(AttendanceLevel.FULL, vm.level(LocalDate.of(2026, 9, 20)))
        assertNotNull(vm.level(today)) // October stays loaded
    }

    @Test
    fun `an error sets the error status and retry recovers`() {
        training.observeDaysError = IllegalStateException("boom")
        training.days[today.minusDays(1)] = workout(4, 4)
        val vm = createViewModel()
        assertEquals(LoadStatus.ERROR, vm.uiState.value.windowStatus)
        assertTrue(vm.uiState.value.levels.isEmpty())

        training.observeDaysError = null
        vm.retry()

        assertEquals(LoadStatus.LOADED, vm.uiState.value.windowStatus)
        assertEquals(AttendanceLevel.FULL, vm.level(today.minusDays(1)))
    }

    @Test
    fun `today's workout is loading, named, rest or unavailable`() {
        val named = createViewModel().apply { }
        assertEquals(TodayWorkout.Rest, named.uiState.value.todayWorkout)

        training.setDay(today, workout(2, 0, name = "Legs"))
        assertEquals(TodayWorkout.Named("Legs"), named.uiState.value.todayWorkout)

        training.setDay(today, TrainingDay(TrainingDay.REST_NAME, TrainingDay.REST_COLOR_HEX))
        assertEquals(TodayWorkout.Rest, named.uiState.value.todayWorkout)
    }

    @Test
    fun `today's workout is unavailable after an error`() {
        training.observeDaysError = IllegalStateException("boom")

        assertEquals(TodayWorkout.Unavailable, createViewModel().uiState.value.todayWorkout)
    }

    @Test
    fun `today's workout shows whatever month is visible`() {
        training.days[today] = workout(2, 0, name = "Legs")
        val vm = createViewModel()

        vm.onVisibleMonthChanged(YearMonth.of(2026, 2))

        assertEquals(TodayWorkout.Named("Legs"), vm.uiState.value.todayWorkout)
    }

    @Test
    fun `tapping another day opens the preview with that day's workout, or rest`() {
        val yesterday = today.minusDays(1)
        training.days[yesterday] = workout(2, 1, name = "Pull")
        val vm = createViewModel()

        vm.onDayClick(yesterday)
        assertEquals(DayPreview(yesterday, training.days.getValue(yesterday)), vm.uiState.value.preview)

        vm.dismissDayPreview()
        assertNull(vm.uiState.value.preview)

        vm.onDayClick(today.plusDays(3))
        assertEquals(TrainingDay.REST, vm.uiState.value.preview?.day)
    }

    @Test
    fun `tapping today or a day of a month that hasn't loaded opens nothing`() {
        val vm = createViewModel()

        vm.onDayClick(today)
        vm.onDayClick(LocalDate.of(2026, 2, 3))

        assertNull(vm.uiState.value.preview)
    }

    @Test
    fun `profile values come from the profile and are none when missing`() {
        val vm = createViewModel()
        assertNull(vm.uiState.value.profile)

        profiles.profile.value = UserProfile.newUser("a@b.c", "A")
        assertEquals(ProfileSummary(age = 18, heightCm = 180, weightKg = 80.0), vm.uiState.value.profile)

        profiles.profile.value = null
        assertNull(vm.uiState.value.profile)
    }

    @Test
    fun `a profile error means no values and no crash`() {
        profiles.observeError = IllegalStateException("boom")

        assertNull(createViewModel().uiState.value.profile)
    }

    @Test
    fun `refreshToday after midnight moves today and recomputes its level`() {
        training.days[today.plusDays(1)] = workout(2, 0)
        val vm = createViewModel()
        assertEquals(AttendanceLevel.PLANNED, vm.level(today.plusDays(1)))

        clock.date = today.plusDays(2)
        vm.refreshToday()

        assertEquals(today.plusDays(2), vm.uiState.value.today)
        assertEquals(AttendanceLevel.MISSED, vm.level(today.plusDays(1)))
        assertEquals(TodayWorkout.Rest, vm.uiState.value.todayWorkout)
    }

    @Test
    fun `refreshToday on the same day changes nothing`() {
        val vm = createViewModel()
        val before = training.observedRanges.size

        vm.refreshToday()

        assertEquals(before, training.observedRanges.size)
    }

    @Test
    fun `home never writes`() {
        training.days[today] = workout(2, 0)
        val vm = createViewModel()
        vm.onDayClick(today.minusDays(1))
        vm.onVisibleMonthChanged(YearMonth.of(2026, 9))
        vm.retry()
        vm.refreshToday()

        assertEquals(0, training.writeCount)
        assertTrue(profiles.createCalls.isEmpty())
    }
}
