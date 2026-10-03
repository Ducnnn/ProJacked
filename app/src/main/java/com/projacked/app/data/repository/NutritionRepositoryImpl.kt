package com.projacked.app.data.repository

import com.projacked.app.data.mapper.toDomain
import com.projacked.app.data.mapper.toDto
import com.projacked.app.data.remote.AuthDataSource
import com.projacked.app.data.remote.FirestoreDataSource
import com.projacked.app.domain.model.Meal
import com.projacked.app.domain.model.MealDay
import com.projacked.app.domain.model.NotSignedInException
import com.projacked.app.domain.repository.NutritionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
) : NutritionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMealDay(date: LocalDate): Flow<MealDay> =
        authDataSource.userId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(MealDay(date))
            } else {
                firestoreDataSource.observeMealDay(uid, date).map { it?.toDomain(date) ?: MealDay(date) }
            }
        }

    override suspend fun addMeal(date: LocalDate, meal: Meal): Result<Unit> = resultOf {
        val uid = authDataSource.currentUserId() ?: throw NotSignedInException()
        firestoreDataSource.addMeal(uid, date, meal.toDto())
    }
}
