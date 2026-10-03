package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Puts a workout on a date. The template is copied, so later edits to the template don't change this date.
 * Assigning [TrainingDay.REST] removes the stored workout instead (a rest day is a date with no document).
 */
class AssignTemplateToDate @Inject constructor(
    private val trainingRepository: TrainingRepository,
) {
    suspend operator fun invoke(date: LocalDate, template: TrainingDay): Result<Unit> =
        if (template.isRest) trainingRepository.deleteDay(date) else trainingRepository.saveDay(date, template)
}
