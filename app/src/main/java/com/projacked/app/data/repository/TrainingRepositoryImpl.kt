package com.projacked.app.data.repository

import com.projacked.app.data.mapper.toDomain
import com.projacked.app.data.mapper.toDto
import com.projacked.app.data.remote.AuthDataSource
import com.projacked.app.data.remote.FirestoreDataSource
import com.projacked.app.domain.model.Exercise
import com.projacked.app.domain.model.NotSignedInException
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.domain.repository.TrainingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
) : TrainingRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTemplates(): Flow<List<TrainingDay>> =
        authDataSource.userId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                firestoreDataSource.observeTemplates(uid).map { templates -> templates.map { it.toDomain() } }
            }
        }

    override suspend fun saveTemplate(template: TrainingDay): Result<Unit> = resultOf {
        requireValidTemplateName(template.name)
        firestoreDataSource.setTemplate(requireUid(), template.name, template.toDto())
    }

    override suspend fun deleteTemplate(name: String): Result<Unit> = resultOf {
        requireValidTemplateName(name)
        firestoreDataSource.deleteTemplate(requireUid(), name)
    }

    override suspend fun getDay(date: LocalDate): Result<TrainingDay> = resultOf {
        firestoreDataSource.getTrainingDay(requireUid(), date)?.toDomain() ?: TrainingDay.REST
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDays(from: LocalDate, toExclusive: LocalDate): Flow<Map<LocalDate, TrainingDay>> =
        authDataSource.userId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyMap())
            } else {
                firestoreDataSource.observeTrainingDays(uid, from, toExclusive).map { days ->
                    days.mapNotNull { (id, dto) -> id.toLocalDateOrNull()?.let { it to dto.toDomain() } }.toMap()
                }
            }
        }

    override suspend fun getDaysFromServer(from: LocalDate, toExclusive: LocalDate): Result<Map<LocalDate, TrainingDay>> =
        resultOf {
            firestoreDataSource.getTrainingDaysFromServer(requireUid(), from, toExclusive)
                .mapNotNull { (id, dto) -> id.toLocalDateOrNull()?.let { it to dto.toDomain() } }
                .toMap()
        }

    override suspend fun saveDays(days: Map<LocalDate, TrainingDay>): Result<Unit> = resultOf {
        firestoreDataSource.writeTrainingDays(
            requireUid(),
            days.mapValues { (_, day) -> if (day.isRest) null else day.toDto() },
        )
    }

    override suspend fun saveDay(date: LocalDate, day: TrainingDay): Result<Unit> = resultOf {
        firestoreDataSource.setTrainingDay(requireUid(), date, day.toDto())
    }

    override suspend fun deleteDay(date: LocalDate): Result<Unit> = resultOf {
        firestoreDataSource.deleteTrainingDay(requireUid(), date)
    }

    override suspend fun updateExercises(date: LocalDate, exercises: List<Exercise>): Result<Unit> = resultOf {
        firestoreDataSource.updateExercises(requireUid(), date, exercises.map { it.toDto() })
    }

    private fun requireUid(): String = authDataSource.currentUserId() ?: throw NotSignedInException()

    /**
     * Templates are stored under their name (kept for compatibility with existing data, decision D7), so a name
     * must be a valid Firestore document id. The UI validates names too (Phase 6); this is the safety net.
     */
    private fun requireValidTemplateName(name: String) {
        require(name.isNotBlank() && '/' !in name && name != "." && name != "..") {
            "\"$name\" can't be used as a template name"
        }
    }

    private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
}
