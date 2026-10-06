package com.projacked.app.fakes

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/** In-memory [TrainingRepository]. A date missing from [days] is a rest day, as in Firestore. */
class FakeTrainingRepository : TrainingRepository {
    val days = mutableMapOf<LocalDate, TrainingDay>()
    val templates = mutableMapOf<String, TrainingDay>()

    /** When set, [getDay] fails for this date. */
    var failingDate: LocalDate? = null

    override fun observeTemplates(): Flow<List<TrainingDay>> = flowOf(templates.values.toList())

    override suspend fun saveTemplate(template: TrainingDay): Result<Unit> {
        templates[template.name] = template
        return Result.success(Unit)
    }

    override suspend fun deleteTemplate(name: String): Result<Unit> {
        templates.remove(name)
        return Result.success(Unit)
    }

    override suspend fun getDay(date: LocalDate): Result<TrainingDay> =
        if (date == failingDate) {
            Result.failure(IllegalStateException("getDay failed for $date"))
        } else {
            Result.success(days[date] ?: TrainingDay.REST)
        }

    private val changes = MutableStateFlow(0)

    /** Every range passed to [observeDays], in order. */
    val observedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()

    /** When set, [observeDays] fails as soon as it is collected. */
    var observeDaysError: Exception? = null

    /** Calls to [saveDay], [deleteDay] and [updateExercises]: Home must never make any. */
    var writeCount = 0
        private set

    /** Live: emits again whenever [setDay], [removeDay] or a write changes [days]. */
    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<Map<LocalDate, TrainingDay>> {
        observedRanges += from to toExclusive
        val error = observeDaysError
        if (error != null) return kotlinx.coroutines.flow.flow { throw error }
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
        setDay(date, day)
        return Result.success(Unit)
    }

    override suspend fun deleteDay(date: LocalDate): Result<Unit> {
        writeCount++
        removeDay(date)
        return Result.success(Unit)
    }

    override suspend fun updateExercises(date: LocalDate, exercises: List<Exercise>): Result<Unit> {
        writeCount++
        val day = days[date] ?: return Result.failure(NoSuchElementException("No workout on $date"))
        setDay(date, day.copy(exercises = exercises))
        return Result.success(Unit)
    }
}
