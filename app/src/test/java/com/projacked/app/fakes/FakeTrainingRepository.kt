package com.projacked.app.fakes

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** In-memory [TrainingRepository]. A date missing from [days] is a rest day, as in Firestore. */
class FakeTrainingRepository : TrainingRepository {
    val days = mutableMapOf<LocalDate, TrainingDay>()

    private val templateStore = MutableStateFlow<Map<String, TrainingDay>>(emptyMap())

    /** The stored templates by name. Use [setTemplate] to change them live. */
    val templates: Map<String, TrainingDay> get() = templateStore.value

    /** When set, [observeTemplates] fails as soon as it is collected. */
    var observeTemplatesError: Exception? = null

    /** When set, [saveDay], [deleteDay], [saveDays], [saveTemplate] and [deleteTemplate] fail and change nothing. */
    var writeError: Exception? = null

    /** When set, [getDaysFromServer] fails (offline, for example). */
    var serverReadError: Exception? = null

    /** When set, [getDaysFromServer] waits for it, so a test can look at the state while a copy is running. */
    var serverReadGate: CompletableDeferred<Unit>? = null

    /** Every range passed to [getDaysFromServer], in order. */
    val serverReads = mutableListOf<Pair<LocalDate, LocalDate>>()

    /** Every map passed to [saveDays], in order (only successful or attempted calls, including failed ones). */
    val savedBatches = mutableListOf<Map<LocalDate, TrainingDay>>()

    override fun observeTemplates(): Flow<List<TrainingDay>> {
        val error = observeTemplatesError
        if (error != null) return flow { throw error }
        return templateStore.map { it.values.toList() }
    }

    /** Changes the templates the way a remote edit would, so live listeners emit. */
    fun setTemplate(template: TrainingDay) {
        templateStore.value = templateStore.value + (template.name to template)
    }

    override suspend fun saveTemplate(template: TrainingDay): Result<Unit> {
        writeError?.let { return Result.failure(it) }
        setTemplate(template)
        return Result.success(Unit)
    }

    override suspend fun deleteTemplate(name: String): Result<Unit> {
        writeError?.let { return Result.failure(it) }
        templateStore.value = templateStore.value - name
        return Result.success(Unit)
    }

    override suspend fun getDay(date: LocalDate): Result<TrainingDay> =
        Result.success(days[date] ?: TrainingDay.REST)

    override suspend fun getDaysFromServer(from: LocalDate, toExclusive: LocalDate): Result<Map<LocalDate, TrainingDay>> {
        serverReads += from to toExclusive
        serverReadGate?.await()
        serverReadError?.let { return Result.failure(it) }
        return Result.success(days.filterKeys { !it.isBefore(from) && it.isBefore(toExclusive) })
    }

    private val changes = MutableStateFlow(0)

    /** Every range passed to [observeDays], in order. */
    val observedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()

    /** When set, [observeDays] fails as soon as it is collected. */
    var observeDaysError: Exception? = null

    /** Calls to [saveDay], [deleteDay], [saveDays] and [updateExercises]: Home must never make any. */
    var writeCount = 0
        private set

    /** Live: emits again whenever [setDay], [removeDay] or a write changes [days]. */
    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<Map<LocalDate, TrainingDay>> {
        observedRanges += from to toExclusive
        val error = observeDaysError
        if (error != null) return flow { throw error }
        return changes.map { days.filterKeys { !it.isBefore(from) && it.isBefore(toExclusive) } }
    }

    /** Changes [days] the way a remote edit would, so live listeners emit. */
    fun setDay(date: LocalDate, day: TrainingDay) {
        days[date] = day
        changes.value++
    }

    fun removeDay(date: LocalDate) {
        days.remove(date)
        changes.value++
    }

    override suspend fun saveDay(date: LocalDate, day: TrainingDay): Result<Unit> {
        writeCount++
        writeError?.let { return Result.failure(it) }
        setDay(date, day)
        return Result.success(Unit)
    }

    override suspend fun deleteDay(date: LocalDate): Result<Unit> {
        writeCount++
        writeError?.let { return Result.failure(it) }
        removeDay(date)
        return Result.success(Unit)
    }

    override suspend fun saveDays(days: Map<LocalDate, TrainingDay>): Result<Unit> {
        writeCount++
        savedBatches += days
        writeError?.let { return Result.failure(it) }
        days.forEach { (date, day) -> if (day.isRest) this.days.remove(date) else this.days[date] = day }
        changes.value++
        return Result.success(Unit)
    }

    override suspend fun updateExercises(date: LocalDate, exercises: List<Exercise>): Result<Unit> {
        writeCount++
        val day = days[date] ?: return Result.failure(NoSuchElementException("No workout on $date"))
        setDay(date, day.copy(exercises = exercises))
        return Result.success(Unit)
    }
}
