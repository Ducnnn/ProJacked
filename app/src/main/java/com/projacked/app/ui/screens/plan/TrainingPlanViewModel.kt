package com.projacked.app.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import com.projacked.app.domain.usecase.AssignTemplateToDate
import com.projacked.app.domain.usecase.CopyPreviousWeek
import com.projacked.app.ui.screens.home.LoadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** How many months the week strip covers before and after the current month. */
private const val STRIP_MONTHS_EACH_WAY = 100L

private const val DAYS_IN_WEEK = 7L

/**
 * Training plan: the week strip with live rings, assigning workouts to dates, copying the previous week and the
 * list of workout templates.
 *
 * The strip loads one live range query for the visible week and one on each side. Dates that have loaded stay in
 * the state, so swiping back and forth doesn't flash. Writes are not awaited by the screen: Firestore applies
 * them locally and the live listeners show the change at once; a failure only sets a message.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrainingPlanViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val assignTemplateToDate: AssignTemplateToDate,
    private val copyPreviousWeek: CopyPreviousWeek,
    private val clock: Clock,
) : ViewModel() {

    /** The dates a query covers; [attempt] changes on retry so the query is made again. */
    private data class Window(val weekStart: LocalDate, val attempt: Int = 0) {
        val from: LocalDate get() = weekStart.minusDays(DAYS_IN_WEEK)
        val toExclusive: LocalDate get() = weekStart.plusDays(2 * DAYS_IN_WEEK)
        val dates: List<LocalDate> get() = generateSequence(from) { it.plusDays(1) }.takeWhile { it < toExclusive }.toList()
    }

    private val initialToday = LocalDate.now(clock)
    private val initialMonth = YearMonth.from(initialToday)

    private val _uiState = MutableStateFlow(
        TrainingPlanUiState(
            today = initialToday,
            startDate = initialMonth.minusMonths(STRIP_MONTHS_EACH_WAY).atDay(1),
            endDate = initialMonth.plusMonths(STRIP_MONTHS_EACH_WAY).atEndOfMonth(),
        ),
    )
    val uiState: StateFlow<TrainingPlanUiState> = _uiState.asStateFlow()

    /** Null until the screen reports the first visible week (the week start depends on the locale). */
    private val windowRequests = MutableStateFlow<Window?>(null)
    private val templateRequests = MutableStateFlow(0)

    init {
        observeWindows()
        observeTemplates()
    }

    /** The strip reports the week it shows; the window moves to it. */
    fun onVisibleWeekChanged(weekStart: LocalDate) {
        _uiState.update { it.copy(visibleWeekStart = weekStart) }
        windowRequests.update { if (it?.weekStart == weekStart) it else Window(weekStart) }
    }

    /** Subscribes to the current window again after an error. */
    fun retryWeek() {
        windowRequests.update { it?.copy(attempt = it.attempt + 1) }
    }

    /** Subscribes to the templates again after an error. */
    fun retryTemplates() {
        templateRequests.update { it + 1 }
    }

    /** Opens the assign dialog for [date]. Dates that haven't loaded and dates outside the strip are ignored. */
    fun onDayClick(date: LocalDate) {
        val state = _uiState.value
        if (date < state.startDate || date > state.endDate || date !in state.loadedDates) return
        _uiState.update { it.copy(dialog = PlanDialog.Assign(date)) }
    }

    /**
     * The assign dialog's choice: a template, or [TrainingDay.REST]. When the date has logged progress, asks first.
     * Choosing Rest for a date with no stored workout writes nothing.
     */
    fun onChoose(day: TrainingDay) {
        val state = _uiState.value
        val date = (state.dialog as? PlanDialog.Assign)?.date ?: return
        if (state.days[date]?.hasLoggedProgress == true) {
            _uiState.update { it.copy(dialog = PlanDialog.ConfirmReplace(date, day)) }
        } else {
            _uiState.update { it.copy(dialog = null) }
            assign(date, day)
        }
    }

    fun confirmReplace() {
        val dialog = _uiState.value.dialog as? PlanDialog.ConfirmReplace ?: return
        _uiState.update { it.copy(dialog = null) }
        assign(dialog.date, dialog.chosen)
    }

    private fun assign(date: LocalDate, day: TrainingDay) {
        if (day.isRest && date !in _uiState.value.days) return
        viewModelScope.launch {
            assignTemplateToDate(date, day).onFailure { showMessage(PlanMessage.SaveFailed) }
        }
    }

    /**
     * Copies the previous week onto the visible week. Does nothing while the week hasn't loaded or a copy is
     * running. Asks first when the week already has a workout.
     */
    fun onCopyClick() {
        val state = _uiState.value
        val weekStart = state.visibleWeekStart ?: return
        if (!state.visibleWeekLoaded || state.copyRunning) return
        val hasWorkout = (0L until DAYS_IN_WEEK).any { state.days[weekStart.plusDays(it)]?.isRest == false }
        if (hasWorkout) {
            _uiState.update { it.copy(dialog = PlanDialog.ConfirmCopy(weekStart)) }
        } else {
            copy(weekStart)
        }
    }

    fun confirmCopy() {
        val dialog = _uiState.value.dialog as? PlanDialog.ConfirmCopy ?: return
        _uiState.update { it.copy(dialog = null) }
        copy(dialog.weekStart)
    }

    private fun copy(weekStart: LocalDate) {
        _uiState.update { it.copy(copyRunning = true) }
        viewModelScope.launch {
            val result = copyPreviousWeek(weekStart)
            _uiState.update {
                it.copy(
                    copyRunning = false,
                    message = if (result.isSuccess) PlanMessage.Copied else PlanMessage.CopyFailed,
                )
            }
        }
    }

    fun onTemplateLongPress(name: String) {
        _uiState.update { it.copy(dialog = PlanDialog.ConfirmDelete(name)) }
    }

    /** Deletes the template only; dates it was already assigned to keep their copy. */
    fun confirmDelete() {
        val dialog = _uiState.value.dialog as? PlanDialog.ConfirmDelete ?: return
        _uiState.update { it.copy(dialog = null) }
        viewModelScope.launch {
            trainingRepository.deleteTemplate(dialog.templateName)
                .onFailure { showMessage(PlanMessage.DeleteFailed(dialog.templateName)) }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    /** Re-reads the clock (the screen calls it on resume), so "today" follows midnight. */
    fun refreshToday() {
        val now = LocalDate.now(clock)
        if (now != _uiState.value.today) _uiState.update { it.copy(today = now) }
    }

    private fun showMessage(message: PlanMessage) {
        _uiState.update { it.copy(message = message) }
    }

    private fun observeWindows() {
        viewModelScope.launch {
            windowRequests.filterNotNull().flatMapLatest { window ->
                trainingRepository.observeDays(window.from, window.toExclusive)
                    .map { days -> Result.success(window to days) }
                    .onStart { if (!_uiState.value.loadedDates.containsAll(window.dates)) setStatus(LoadStatus.LOADING) }
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
        _uiState.update { state ->
            val kept = state.days.filterKeys { it < window.from || it >= window.toExclusive }
            state.copy(
                days = kept + days,
                loadedDates = state.loadedDates + window.dates,
                windowStatus = LoadStatus.LOADED,
            )
        }
    }

    private fun setStatus(status: LoadStatus) {
        _uiState.update { it.copy(windowStatus = status) }
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            templateRequests.flatMapLatest {
                trainingRepository.observeTemplates()
                    .map<List<TrainingDay>, TemplatesState> { list ->
                        TemplatesState.Loaded(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
                    }
                    .catch { emit(TemplatesState.Error) }
            }.collect { templates -> _uiState.update { it.copy(templates = templates) } }
        }
    }
}
