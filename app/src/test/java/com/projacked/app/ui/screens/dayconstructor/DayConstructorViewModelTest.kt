package com.projacked.app.ui.screens.dayconstructor

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.MuscleGroup
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.model.WorkoutSet
import com.projacked.app.domain.usecase.TemplateNameError
import com.projacked.app.domain.usecase.ValidateTemplateName
import com.projacked.app.fakes.FakeTrainingRepository
import com.projacked.app.fakes.MainDispatcherRule
import com.projacked.app.ui.screens.plan.TemplatesState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayConstructorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val training = FakeTrainingRepository()
    private val push = TrainingDay("Push", "#e53935")
    private val pull = TrainingDay("pull", "#1E88E5")

    private fun create(vararg templates: TrainingDay): DayConstructorViewModel {
        templates.forEach(training::setTemplate)
        return DayConstructorViewModel(training, ValidateTemplateName())
    }

    private val DayConstructorViewModel.state get() = uiState.value

    private fun DayConstructorViewModel.addExercise(name: String, muscle: MuscleGroup = MuscleGroup.CHEST) {
        onAddExerciseClick()
        onEditorNameChange(name)
        onEditorMuscleChange(muscle)
        confirmExercise()
    }

    @Test
    fun `starts empty then the default colour follows the templates`() {
        val vm = create(push, pull)
        assertTrue(vm.state.templates is TemplatesState.Loaded)
        assertEquals("", vm.state.name)
        assertEquals("#d81b60", vm.state.colorHex)
        assertFalse(vm.state.hasChanges)
    }

    @Test
    fun `a template added live moves the default until a colour is picked`() {
        val vm = create(push)
        assertEquals("#d81b60", vm.state.colorHex)
        training.setTemplate(TrainingDay("Pink", "#d81b60"))
        assertEquals("#8e24aa", vm.state.colorHex)
        vm.onColorPicked("#00897b")
        training.setTemplate(TrainingDay("Purple", "#8e24aa"))
        assertEquals("#00897b", vm.state.colorHex)
    }

    @Test
    fun `a templates error blocks saving and retry recovers`() {
        training.observeTemplatesError = IllegalStateException("boom")
        val vm = create()
        assertTrue(vm.state.templates is TemplatesState.Error)
        vm.onNameChange("Arms")
        vm.onSaveClick()
        assertTrue(training.savedTemplates.isEmpty())
        assertFalse(vm.state.saved)

        training.observeTemplatesError = null
        vm.retryTemplates()
        assertTrue(vm.state.templates is TemplatesState.Loaded)
        vm.onSaveClick()
        assertEquals(1, training.savedTemplates.size)
    }

    @Test
    fun `the name stops at 40 code points without cutting a surrogate pair`() {
        val vm = create()
        val emoji = "💪"
        vm.onNameChange(emoji.repeat(45))
        assertEquals(emoji.repeat(40), vm.state.name)
        vm.onNameChange("a".repeat(50))
        assertEquals(40, vm.state.name.length)
    }

    @Test
    fun `name errors show on save and typing clears them`() {
        val vm = create()
        vm.onSaveClick()
        assertEquals(TemplateNameError.BLANK, vm.state.nameError)
        vm.onNameChange("a")
        assertNull(vm.state.nameError)
        vm.onNameChange("Push/Pull")
        vm.onSaveClick()
        assertEquals(TemplateNameError.CONTAINS_SLASH, vm.state.nameError)
        vm.onNameChange("__x__")
        vm.onSaveClick()
        assertEquals(TemplateNameError.RESERVED, vm.state.nameError)
        assertTrue(training.savedTemplates.isEmpty())
    }

    @Test
    fun `adding an exercise needs a name`() {
        val vm = create()
        vm.onAddExerciseClick()
        vm.confirmExercise()
        assertTrue((vm.state.dialog as ConstructorDialog.ExerciseEditor).showNameError)
        assertTrue(vm.state.exercises.isEmpty())
        vm.onEditorNameChange("x")
        assertFalse((vm.state.dialog as ConstructorDialog.ExerciseEditor).showNameError)
    }

    @Test
    fun `a new exercise is trimmed and starts with one empty set`() {
        val vm = create()
        vm.onAddExerciseClick()
        vm.onEditorNameChange("  Bench press ")
        vm.onEditorMuscleChange(MuscleGroup.SHOULDERS)
        vm.onEditorNotesChange(" Slow ")
        vm.confirmExercise()
        assertNull(vm.state.dialog)
        assertEquals(listOf(Exercise("Bench press", MuscleGroup.SHOULDERS, "Slow")), vm.state.exercises)
        assertEquals(listOf(WorkoutSet()), vm.state.exercises.single().sets)
        assertTrue(vm.state.hasChanges)
    }

    @Test
    fun `editing keeps the sets and completion`() {
        val vm = create()
        vm.addExercise("A")
        vm.addExercise("B")
        vm.onExerciseClick(1)
        val editor = vm.state.dialog as ConstructorDialog.ExerciseEditor
        assertEquals("B", editor.name)
        vm.onEditorNameChange("B2")
        vm.onEditorMuscleChange(MuscleGroup.LEGS)
        vm.confirmExercise()
        assertEquals(listOf("A", "B2"), vm.state.exercises.map { it.name })
        assertEquals(MuscleGroup.LEGS, vm.state.exercises[1].muscleGroup)
        assertEquals(listOf(WorkoutSet()), vm.state.exercises[1].sets)
    }

    @Test
    fun `deleting removes the item at the current index`() {
        val vm = create()
        vm.addExercise("Row")
        vm.addExercise("Row", MuscleGroup.BACK)
        vm.onDeleteExercise(1)
        assertEquals(listOf(MuscleGroup.CHEST), vm.state.exercises.map { it.muscleGroup })
    }

    @Test
    fun `picking a colour sets it and closes the dialog`() {
        val vm = create()
        vm.onColorClick()
        assertEquals(ConstructorDialog.ColorPicker, vm.state.dialog)
        vm.onColorPicked("#00897b")
        assertEquals("#00897b", vm.state.colorHex)
        assertNull(vm.state.dialog)
        assertFalse(vm.state.hasChanges)
    }

    @Test
    fun `saving stores the trimmed name colour and exercises once`() {
        val vm = create(push)
        vm.onNameChange("  Arms day  ")
        vm.addExercise("Curl", MuscleGroup.ARMS)
        vm.addExercise("Dips", MuscleGroup.ARMS)
        vm.onColorPicked("#00897b")
        vm.onSaveClick()
        assertTrue(vm.state.saved)
        val saved = training.savedTemplates.single()
        assertEquals("Arms day", saved.name)
        assertEquals("#00897b", saved.colorHex)
        assertEquals(listOf("Curl", "Dips"), saved.exercises.map { it.name })
        vm.onSaveClick()
        assertEquals(1, training.savedTemplates.size)
    }

    @Test
    fun `a workout without exercises can be saved`() {
        val vm = create()
        vm.onNameChange("Cardio")
        vm.onSaveClick()
        assertTrue(vm.state.saved)
        assertTrue(training.savedTemplates.single().exercises.isEmpty())
    }

    @Test
    fun `the same name asks before replacing`() {
        val vm = create(push)
        vm.onNameChange("Push")
        vm.onSaveClick()
        assertEquals(ConstructorDialog.ConfirmReplace("Push"), vm.state.dialog)
        vm.dismissDialog()
        assertTrue(training.savedTemplates.isEmpty())
        vm.onSaveClick()
        vm.confirmReplace()
        assertTrue(vm.state.saved)
        assertEquals("Push", training.savedTemplates.single().name)
    }

    @Test
    fun `a name that differs in case saves straight away`() {
        val vm = create(push)
        vm.onNameChange("push")
        vm.onSaveClick()
        assertTrue(vm.state.saved)
        assertEquals("push", training.savedTemplates.single().name)
    }

    @Test
    fun `a write that fails at once shows a message and stays`() {
        training.writeError = IllegalStateException("no")
        val vm = create()
        vm.onNameChange("Arms")
        vm.onSaveClick()
        assertFalse(vm.state.saved)
        assertEquals(DayConstructorMessage.SAVE_FAILED, vm.state.message)
        vm.onMessageShown()
        assertNull(vm.state.message)
    }

    @Test
    fun `back asks only when something was entered`() {
        val vm = create()
        vm.onColorPicked("#00897b")
        assertFalse(vm.state.hasChanges)
        vm.onNameChange("x")
        assertTrue(vm.state.hasChanges)
        vm.onBackWithChanges()
        assertEquals(ConstructorDialog.ConfirmDiscard, vm.state.dialog)
        vm.dismissDialog()
        assertFalse(vm.state.discarded)
        vm.onBackWithChanges()
        vm.confirmDiscard()
        assertTrue(vm.state.discarded)
        assertTrue(training.savedTemplates.isEmpty())
    }

    @Test
    fun `the screen never writes training days`() {
        val vm = create(push)
        vm.onNameChange("Arms")
        vm.onSaveClick()
        assertEquals(0, training.writeCount)
    }
}
