package com.projacked.app.ui.screens.dayconstructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MAX_TEMPLATE_NAME_LENGTH
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import com.projacked.app.domain.usecase.TemplateNameResult
import com.projacked.app.domain.usecase.ValidateTemplateName
import com.projacked.app.ui.screens.plan.TemplatesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_EXERCISE_NAME_LENGTH = 40
private const val MAX_NOTES_LENGTH = 200

/**
 * Day constructor: builds a new workout template (name, colour, exercises) and saves it to `CustomDays`.
 *
 * Saving isn't awaited (P6-10): Firestore applies the write locally and the screen returns to the plan, which
 * shows the workout at once. Only a failure that happens straight away (not signed in, name refused) is reported.
 * Creating only: editing an existing workout is a later stage (P6-4).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DayConstructorViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val validateTemplateName: ValidateTemplateName,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayConstructorUiState())
    val uiState: StateFlow<DayConstructorUiState> = _uiState.asStateFlow()

    private val templateRequests = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            templateRequests.flatMapLatest {
                trainingRepository.observeTemplates()
                    .map<List<TrainingDay>, TemplatesState> { TemplatesState.Loaded(it) }
                    .catch { emit(TemplatesState.Error) }
            }.collect { templates ->
                _uiState.update { state ->
                    val color = if (!state.colorPicked && templates is TemplatesState.Loaded) {
                        defaultTemplateColor(templates.templates.map { it.colorHex })
                    } else {
                        state.colorHex
                    }
                    state.copy(templates = templates, colorHex = color)
                }
            }
        }
    }

    fun retryTemplates() {
        _uiState.update { it.copy(templates = TemplatesState.Loading) }
        templateRequests.value++
    }

    fun onNameChange(text: String) {
        _uiState.update { it.copy(name = text.takeCodePoints(MAX_TEMPLATE_NAME_LENGTH), nameError = null) }
    }

    // Exercises

    fun onAddExerciseClick() {
        _uiState.update {
            it.copy(dialog = ConstructorDialog.ExerciseEditor(null, "", MuscleGroup.CHEST, ""))
        }
    }

    fun onExerciseClick(index: Int) {
        _uiState.update { state ->
            val exercise = state.exercises.getOrNull(index) ?: return@update state
            state.copy(
                dialog = ConstructorDialog.ExerciseEditor(index, exercise.name, exercise.muscleGroup, exercise.notes),
            )
        }
    }

    fun onEditorNameChange(text: String) = updateEditor {
        it.copy(name = text.takeCodePoints(MAX_EXERCISE_NAME_LENGTH), showNameError = false)
    }

    fun onEditorMuscleChange(group: MuscleGroup) = updateEditor { it.copy(muscleGroup = group) }

    fun onEditorNotesChange(text: String) = updateEditor { it.copy(notes = text.takeCodePoints(MAX_NOTES_LENGTH)) }

    fun confirmExercise() {
        _uiState.update { state ->
            val editor = state.dialog as? ConstructorDialog.ExerciseEditor ?: return@update state
            val name = editor.name.trim()
            if (name.isEmpty()) return@update state.copy(dialog = editor.copy(showNameError = true))
            val notes = editor.notes.trim()
            val index = editor.index
            val exercises = if (index == null || index !in state.exercises.indices) {
                state.exercises + Exercise(name = name, muscleGroup = editor.muscleGroup, notes = notes)
            } else {
                state.exercises.toMutableList().also {
                    it[index] = it[index].copy(name = name, muscleGroup = editor.muscleGroup, notes = notes)
                }
            }
            state.copy(exercises = exercises, dialog = null)
        }
    }

    fun onDeleteExercise(index: Int) {
        _uiState.update { state ->
            if (index !in state.exercises.indices) state
            else state.copy(exercises = state.exercises.filterIndexed { i, _ -> i != index })
        }
    }

    // Colour

    fun onColorClick() {
        _uiState.update { it.copy(dialog = ConstructorDialog.ColorPicker) }
    }

    fun onColorPicked(hex: String) {
        _uiState.update { it.copy(colorHex = hex, colorPicked = true, dialog = null) }
    }

    // Saving

    fun onSaveClick() {
        val state = _uiState.value
        val templates = state.templates as? TemplatesState.Loaded ?: return
        if (state.saved) return
        when (val result = validateTemplateName(state.name)) {
            is TemplateNameResult.Invalid -> _uiState.update { it.copy(nameError = result.error) }
            is TemplateNameResult.Valid -> {
                if (templates.templates.any { it.name == result.name }) {
                    _uiState.update { it.copy(dialog = ConstructorDialog.ConfirmReplace(result.name)) }
                } else {
                    write(result.name)
                }
            }
        }
    }

    fun confirmReplace() {
        val state = _uiState.value
        val dialog = state.dialog as? ConstructorDialog.ConfirmReplace ?: return
        _uiState.update { it.copy(dialog = null) }
        if (!state.saved) write(dialog.name)
    }

    /**
     * Starts the write and doesn't wait for the server. The scope's Main.immediate dispatcher runs it up to its
     * first suspension, so a failure that happens at once is already known when [launch] returns.
     */
    private fun write(name: String) {
        val state = _uiState.value
        var failedAtOnce = false
        viewModelScope.launch {
            val result = trainingRepository.saveTemplate(TrainingDay(name, state.colorHex, state.exercises))
            if (result.isFailure) failedAtOnce = true
        }
        _uiState.update {
            if (failedAtOnce) it.copy(message = DayConstructorMessage.SAVE_FAILED) else it.copy(saved = true)
        }
    }

    // Back

    fun onBackWithChanges() {
        _uiState.update { it.copy(dialog = ConstructorDialog.ConfirmDiscard) }
    }

    fun confirmDiscard() {
        _uiState.update { it.copy(dialog = null, discarded = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun updateEditor(change: (ConstructorDialog.ExerciseEditor) -> ConstructorDialog.ExerciseEditor) {
        _uiState.update { state ->
            val editor = state.dialog as? ConstructorDialog.ExerciseEditor ?: return@update state
            state.copy(dialog = change(editor))
        }
    }
}

/** At most [max] code points, never cutting a surrogate pair. */
private fun String.takeCodePoints(max: Int): String =
    if (codePointCount(0, length) <= max) this else substring(0, offsetByCodePoints(0, max))
