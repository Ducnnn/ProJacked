package com.projacked.app.ui.screens.currentday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.SetInput
import com.projacked.app.domain.repository.TrainingRepository
import com.projacked.app.domain.usecase.ParseReps
import com.projacked.app.domain.usecase.ParseWeight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val TYPING_SAVE_DELAY_MS = 1_000L
private const val MAX_FIELD_LENGTH = 10

/**
 * The workout logger for today. The date is read once when the screen opens and never again (P7-4), so every save
 * goes to the day that was loaded, even past midnight.
 *
 * Every edit names its exercise by index, and each card's state lives here, so values can't leak between
 * exercises (the old adapter's bug). Typing saves 1 s after the last keystroke; "+", delete and Finished save at
 * once; [flushPendingSave] saves what is still pending when the screen stops. Saves only `update` the
 * `exercises` field and never run on a rest day.
 */
@HiltViewModel
class CurrentDayViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    clock: Clock,
    private val parseReps: ParseReps,
    private val parseWeight: ParseWeight,
) : ViewModel() {

    private val date: LocalDate = LocalDate.now(clock)

    private val _uiState = MutableStateFlow(CurrentDayUiState(date = date))
    val uiState: StateFlow<CurrentDayUiState> = _uiState.asStateFlow()

    private var typingTimer: Job? = null

    init {
        load()
    }

    /** Loads today again after an error. Does nothing in any other state. */
    fun retry() {
        if (_uiState.value.load != CurrentDayLoad.Error) return
        load()
    }

    private fun load() {
        _uiState.update { it.copy(load = CurrentDayLoad.Loading) }
        viewModelScope.launch {
            trainingRepository.getDay(date).fold(
                onSuccess = { day ->
                    _uiState.update {
                        if (day.exercises.isEmpty()) {
                            it.copy(load = CurrentDayLoad.Rest, cards = emptyList())
                        } else {
                            it.copy(
                                load = CurrentDayLoad.Loaded(day.name),
                                cards = day.exercises.map(ExerciseCardState::of),
                            )
                        }
                    }
                },
                onFailure = { _uiState.update { it.copy(load = CurrentDayLoad.Error) } },
            )
        }
    }

    fun onRepsChange(index: Int, text: String) {
        val typed = text.take(MAX_FIELD_LENGTH)
        val parsed = parseReps(typed)
        val edited = editCard(index) { card ->
            when (parsed) {
                is SetInput.Valid -> {
                    val set = card.exercise.sets[card.selectedSet].copy(reps = parsed.value)
                    card.copy(
                        exercise = card.exercise.updateSet(card.selectedSet, set),
                        repsText = typed,
                        repsError = null,
                    )
                }
                is SetInput.Invalid -> card.copy(repsText = typed, repsError = parsed.error)
            }
        }
        if (edited && parsed is SetInput.Valid) scheduleTypingSave()
    }

    fun onWeightChange(index: Int, text: String) {
        val typed = text.take(MAX_FIELD_LENGTH)
        val parsed = parseWeight(typed)
        val edited = editCard(index) { card ->
            when (parsed) {
                is SetInput.Valid -> {
                    val set = card.exercise.sets[card.selectedSet].copy(weightKg = parsed.value)
                    card.copy(
                        exercise = card.exercise.updateSet(card.selectedSet, set),
                        weightText = typed,
                        weightError = null,
                    )
                }
                is SetInput.Invalid -> card.copy(weightText = typed, weightError = parsed.error)
            }
        }
        if (edited && parsed is SetInput.Valid) scheduleTypingSave()
    }

    /** Shows another set's values. Works on a finished card too (P7-7); nothing is saved. */
    fun onSelectSet(index: Int, setIndex: Int) {
        val card = loadedCard(index) ?: return
        if (setIndex !in card.exercise.sets.indices) return
        replaceCard(index) { it.withSelected(setIndex) }
    }

    /** "+": saves the current set and starts the next one. Does nothing unless [ExerciseCardState.canAddSet]. */
    fun onAddSet(index: Int) {
        val card = loadedCard(index) ?: return
        if (!card.canAddSet) return
        val last = card.exercise.sets.lastIndex
        val exercise = card.exercise.commitSetAndAddNext(last, card.exercise.sets[last])
        replaceCard(index) { ExerciseCardState(exercise, 0, "", "").withSelected(exercise.sets.lastIndex) }
        saveNow()
    }

    /** Removes the last set, or clears the only one. Does nothing unless [ExerciseCardState.canDeleteSet]. */
    fun onDeleteSet(index: Int) {
        val card = loadedCard(index) ?: return
        if (!card.canDeleteSet) return
        val wasOnlySet = card.exercise.sets.size == 1
        val wasLast = card.selectedSet == card.exercise.sets.lastIndex
        val exercise = card.exercise.removeLastSet()
        replaceCard(index) {
            val updated = card.copy(exercise = exercise)
            when {
                wasOnlySet -> updated.withSelected(0)
                wasLast -> updated.withSelected(exercise.sets.lastIndex)
                else -> updated
            }
        }
        saveNow()
    }

    /** Finished toggle. Both fields reload from the model, so invalid text is dropped. */
    fun onFinishedChange(index: Int, finished: Boolean) {
        val card = loadedCard(index) ?: return
        replaceCard(index) {
            card.copy(exercise = card.exercise.withCompleted(finished)).withSelected(card.selectedSet)
        }
        saveNow()
    }

    /** Saves at once if a typing save is waiting. The screen calls this when it stops (Back, Home, other app). */
    fun flushPendingSave() {
        if (typingTimer?.isActive == true) saveNow()
    }

    /** Saves the current list again after a failure. */
    fun retrySave() = saveNow()

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadedCard(index: Int): ExerciseCardState? =
        _uiState.value.takeIf { it.load is CurrentDayLoad.Loaded }?.cards?.getOrNull(index)

    /** Applies [transform] to an unfinished card of the loaded screen. False when the edit was ignored. */
    private fun editCard(index: Int, transform: (ExerciseCardState) -> ExerciseCardState): Boolean {
        val card = loadedCard(index) ?: return false
        if (!card.inputsEnabled) return false
        replaceCard(index, transform)
        return true
    }

    private fun replaceCard(index: Int, transform: (ExerciseCardState) -> ExerciseCardState) {
        _uiState.update { state ->
            state.copy(cards = state.cards.mapIndexed { i, card -> if (i == index) transform(card) else card })
        }
    }

    private fun scheduleTypingSave() {
        typingTimer?.cancel()
        typingTimer = viewModelScope.launch {
            delay(TYPING_SAVE_DELAY_MS.milliseconds)
            typingTimer = null
            saveNow()
        }
    }

    /**
     * Saves the whole current list now. The save runs in its own coroutine, so cancelling a typing timer never
     * cancels one that has started, and with `Dispatchers.Main.immediate` the Firestore update is issued before
     * this returns, even when the screen is leaving.
     */
    private fun saveNow() {
        typingTimer?.cancel()
        typingTimer = null
        val state = _uiState.value
        if (state.load !is CurrentDayLoad.Loaded) return
        val exercises = state.cards.map { it.exercise }
        viewModelScope.launch {
            trainingRepository.updateExercises(date, exercises).onFailure {
                _uiState.update { it.copy(message = CurrentDayMessage.SAVE_FAILED) }
            }
        }
    }
}
