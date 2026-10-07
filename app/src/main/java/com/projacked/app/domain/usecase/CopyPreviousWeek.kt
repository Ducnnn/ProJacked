package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Copies each of the 7 days starting at `weekStart` from the same weekday one week earlier. Logged values and
 * completion are cleared but the number of sets is kept. Rest days copy as rest days, which deletes the target
 * date's document.
 *
 * All or nothing: the earlier week is read from the server (so this needs a connection), and if that fails nothing
 * is written. Otherwise the 7 days are written in one batch.
 */
class CopyPreviousWeek @Inject constructor(
    private val trainingRepository: TrainingRepository,
) {
    suspend operator fun invoke(weekStart: LocalDate): Result<Unit> {
        val source = trainingRepository.getDaysFromServer(weekStart.minusDays(DAYS_IN_WEEK), weekStart)
            .getOrElse { return Result.failure(it) }
        val targets = (0 until DAYS_IN_WEEK).associate { offset ->
            val date = weekStart.plusDays(offset)
            date to (source[date.minusDays(DAYS_IN_WEEK)] ?: TrainingDay.REST).resetProgress()
        }
        return trainingRepository.saveDays(targets)
    }

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
