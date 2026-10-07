package com.projacked.app.domain.repository

import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.TrainingDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Workout templates (`CustomDays`, identified by name) and the workouts assigned to dates (`TrainingDays`).
 * A date without a stored workout is a rest day.
 */
interface TrainingRepository {

    /** Live list of templates. Empty when nobody is signed in. */
    fun observeTemplates(): Flow<List<TrainingDay>>

    /** Saves a template under its name, replacing any template with the same name. */
    suspend fun saveTemplate(template: TrainingDay): Result<Unit>

    suspend fun deleteTemplate(name: String): Result<Unit>

    /** The workout on [date], or [TrainingDay.REST] when nothing is stored. */
    suspend fun getDay(date: LocalDate): Result<TrainingDay>

    /**
     * Live workouts for dates in [from, toExclusive), loaded with a single query.
     * Only dates that have a stored workout appear in the map; every other date is a rest day.
     */
    fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<Map<LocalDate, TrainingDay>>

    suspend fun saveDay(date: LocalDate, day: TrainingDay): Result<Unit>

    /**
     * One range read of [from, toExclusive) from the server only, so it fails when offline. Only dates with a
     * stored workout appear. It never comes from the cache, so a copy can't act on stale or empty cached data.
     */
    suspend fun getDaysFromServer(from: LocalDate, toExclusive: LocalDate): Result<Map<LocalDate, TrainingDay>>

    /**
     * Writes every entry in one atomic batch: all or nothing. A day with [TrainingDay.isRest] deletes that date's
     * document; any other day replaces it, as [saveDay] does.
     */
    suspend fun saveDays(days: Map<LocalDate, TrainingDay>): Result<Unit>

    /** Removes the workout from [date], making it a rest day. */
    suspend fun deleteDay(date: LocalDate): Result<Unit>

    /** Saves logged sets and completion for [date]. Fails if [date] has no stored workout. */
    suspend fun updateExercises(date: LocalDate, exercises: List<Exercise>): Result<Unit>
}
