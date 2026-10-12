package com.projacked.app.ui.screens.dayconstructor

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.usecase.TemplateNameError
import com.projacked.app.ui.screens.plan.TemplatesState

/** The dialog that is open, if any. Lives in the state so it survives rotation. */
sealed interface ConstructorDialog {
    /** Adding ([index] null) or editing the exercise at [index]. */
    data class ExerciseEditor(
        val index: Int?,
        val name: String,
        val muscleGroup: MuscleGroup,
        val notes: String,
        val showNameError: Boolean = false,
    ) : ConstructorDialog

    data object ColorPicker : ConstructorDialog

    /** A saved workout is called [name] already. */
    data class ConfirmReplace(val name: String) : ConstructorDialog

    data object ConfirmDiscard : ConstructorDialog
}

/** One-off messages. The screen shows one and calls `onMessageShown()`. */
enum class DayConstructorMessage { SAVE_FAILED }

data class DayConstructorUiState(
    val name: String = "",
    val nameError: TemplateNameError? = null,
    val colorHex: String = TemplatePalette.first().hex,
    val colorPicked: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val templates: TemplatesState = TemplatesState.Loading,
    val dialog: ConstructorDialog? = null,
    val message: DayConstructorMessage? = null,
    val saved: Boolean = false,
    val discarded: Boolean = false,
) {
    /** A name or an exercise has been entered. A colour change on its own doesn't count. */
    val hasChanges: Boolean
        get() = name.isNotBlank() || exercises.isNotEmpty()
}
