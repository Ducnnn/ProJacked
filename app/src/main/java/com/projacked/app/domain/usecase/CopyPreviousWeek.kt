package com.projacked.app.domain.usecase

import com.projacked.app.domain.repository.TrainingRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Copies each of the 7 days starting at `weekStart` from the same weekday one week earlier. Logged values and
 * completion are cleared but the number of sets is kept. Rest days copy as rest days.
 * Stops at the first failure and returns it.
 */
class CopyPreviousWeek @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val assignTemplateToDate: AssignTemplateToDate,
) {
    suspend operator fun invoke(weekStart: LocalDate): Result<Unit> {
        for (offset in 0L until DAYS_IN_WEEK) {
            val date = weekStart.plusDays(offset)
            val previous = trainingRepository.getDay(date.minusWeeks(1)).getOrElse { return Result.failure(it) }
            assignTemplateToDate(date, previous.resetProgress()).onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
