package com.projacked.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.ProfileRepository
import com.projacked.app.domain.repository.TrainingRepository
import com.projacked.app.domain.usecase.ComputeAttendance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** How many months the calendar covers before and after the current month. */
private const val CALENDAR_MONTHS_EACH_WAY = 100L

/**
 * Home only reads (`TrainingDays` and `users/{uid}`); nothing here writes to Firestore or touches nutrition data.
 *
 * The heatmap loads one live range query per window of three months (the visible month and one on each side).
 * Months that have loaded stay in the state, so swiping back and forth doesn't flash.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val profileRepository: ProfileRepository,
    private val computeAttendance: ComputeAttendance,
    private val clock: Clock,
) : ViewModel() {

    /** The months a query covers; [attempt] changes on retry so the query is made again. */
    private data class Window(val center: YearMonth, val attempt: Int = 0) {
        val months: List<YearMonth> get() = listOf(center.minusMonths(1), center, center.plusMonths(1))
        val from: LocalDate get() = center.minusMonths(1).atDay(1)
        val toExclusive: LocalDate get() = center.plusMonths(1).atEndOfMonth().plusDays(1)
    }

    private val loadedMonths = mutableSetOf<YearMonth>()

    private val initialToday = LocalDate.now(clock)
    private val initialMonth = YearMonth.from(initialToday)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            today = initialToday,
            startMonth = initialMonth.minusMonths(CALENDAR_MONTHS_EACH_WAY),
            endMonth = initialMonth.plusMonths(CALENDAR_MONTHS_EACH_WAY),
            visibleMonth = initialMonth,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val windowRequests = MutableStateFlow(Window(initialMonth))
    private val todayRequests = MutableStateFlow(initialToday)

    init {
        observeWindows()
        observeToday()
        observeProfile()
    }

    /** The calendar reports the month it shows; the window moves to it. */
    fun onVisibleMonthChanged(month: YearMonth) {
        _uiState.update { it.copy(visibleMonth = month) }
        windowRequests.update { if (it.center == month) it else Window(month) }
    }

    /** Subscribes to the current window again after an error. */
    fun retry() {
        windowRequests.update { it.copy(attempt = it.attempt + 1) }
    }

    /**
     * Opens the preview for [date], from the data already loaded. Today never opens it: the screen opens the
     * logger instead. Dates of months that haven't loaded are ignored.
     */
    fun onDayClick(date: LocalDate) {
        val state = _uiState.value
        if (date == state.today || YearMonth.from(date) !in loadedMonths) return
        _uiState.update { it.copy(preview = DayPreview(date, it.days[date] ?: TrainingDay.REST)) }
    }

    fun dismissDayPreview() {
        _uiState.update { it.copy(preview = null) }
    }

    /** Re-reads the clock (the screen calls it on resume); when the date changed, today's data follows. */
    fun refreshToday() {
        val now = LocalDate.now(clock)
        if (now == _uiState.value.today) return
        _uiState.update { withLevels(it.copy(today = now, todayWorkout = TodayWorkout.Loading)) }
        todayRequests.value = now
    }

    private fun observeWindows() {
        viewModelScope.launch {
            windowRequests.flatMapLatest { window ->
                trainingRepository.observeDays(window.from, window.toExclusive)
                    .map { days -> Result.success(window to days) }
                    .onStart { if (!loadedMonths.containsAll(window.months)) setStatus(LoadStatus.LOADING) }
                    .catch { emit(Result.failure(it)) }
            }.collect { result ->
                result.fold(
                    onSuccess = { (window, days) -> applyWindow(window, days) },
                    onFailure = { setStatus(LoadStatus.ERROR) },
                )
            }
        }
    }

    private fun applyWindow(window: Window, days: Map<LocalDate, TrainingDay>) {
        loadedMonths += window.months
        _uiState.update { state ->
            val kept = state.days.filterKeys { it < window.from || it >= window.toExclusive }
            withLevels(state.copy(days = kept + days, windowStatus = LoadStatus.LOADED))
        }
    }

    private fun setStatus(status: LoadStatus) {
        _uiState.update { it.copy(windowStatus = status) }
    }

    private fun withLevels(state: HomeUiState): HomeUiState {
        val levels = buildMap {
            for (month in loadedMonths) {
                for (day in 1..month.lengthOfMonth()) {
                    val date = month.atDay(day)
                    put(date, computeAttendance(state.days[date] ?: TrainingDay.REST, date, state.today))
                }
            }
        }
        return state.copy(levels = levels)
    }

    private fun observeToday() {
        viewModelScope.launch {
            todayRequests.flatMapLatest { today ->
                trainingRepository.observeDays(today, today.plusDays(1))
                    .map { days ->
                        val day = days[today]
                        if (day == null || day.exercises.isEmpty()) TodayWorkout.Rest else TodayWorkout.Named(day.name)
                    }
                    .catch { emit(TodayWorkout.Unavailable) }
            }.collect { workout -> _uiState.update { it.copy(todayWorkout = workout) } }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.observeProfile()
                .map { profile ->
                    profile?.let { ProfileSummary(it.parameters.age, it.parameters.heightCm, it.parameters.weightKg) }
                }
                .catch { emit(null) }
                .collect { summary -> _uiState.update { it.copy(profile = summary) } }
        }
    }
}
