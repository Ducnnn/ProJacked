package com.projacked.app.fakes

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
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

    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<Map<LocalDate, TrainingDay>> =
        flowOf(days.filterKeys { !it.isBefore(from) && it.isBefore(toExclusive) })

    override suspend fun saveDay(date: LocalDate, day: TrainingDay): Result<Unit> {
        days[date] = day
        return Result.success(Unit)
    }

    override suspend fun deleteDay(date: LocalDate): Result<Unit> {
        days.remove(date)
        return Result.success(Unit)
    }

    override suspend fun updateExercises(date: LocalDate, exercises: List<Exercise>): Result<Unit> {
        val day = days[date] ?: return Result.failure(NoSuchElementException("No workout on $date"))
        days[date] = day.copy(exercises = exercises)
        return Result.success(Unit)
    }
}
