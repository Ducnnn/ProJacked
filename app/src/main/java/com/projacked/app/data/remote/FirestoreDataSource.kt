package com.projacked.app.data.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import com.projacked.app.data.remote.dto.ExerciseDto
import com.projacked.app.data.remote.dto.MealDayDto
import com.projacked.app.data.remote.dto.MealDto
import com.projacked.app.data.remote.dto.TrainingDayDto
import com.projacked.app.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and writes Firestore documents as DTOs. The only class that talks to [FirebaseFirestore].
 * Callers pass the signed-in user's id; repositories handle the "not signed in" case.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val db: FirebaseFirestore,
) {
    // users/{uid}

    fun observeUser(uid: String): Flow<UserDto?> =
        db.document(FirestorePaths.user(uid)).snapshots().map { it.toObject(UserDto::class.java) }

    suspend fun setUser(uid: String, user: UserDto) {
        db.document(FirestorePaths.user(uid)).set(user).await()
    }

    /**
     * Creates `users/{uid}` in a transaction only if it's missing. Returns true when it created it. Needs the server,
     * so it fails offline. `createdAt` stays null, so `@ServerTimestamp` fills it in.
     */
    suspend fun createUserIfMissing(uid: String, user: UserDto): Boolean {
        val ref = db.document(FirestorePaths.user(uid))
        return db.runTransaction { transaction ->
            if (transaction.get(ref).exists()) {
                false
            } else {
                transaction.set(ref, user)
                true
            }
        }.await()
    }

    /**
     * Writes [fields] and leaves the rest of the document alone. Creates the document if it doesn't exist: the old
     * app wrote it without checking, so some accounts may have none (an `update` would fail for them).
     */
    suspend fun mergeUser(uid: String, fields: Map<String, Any>) {
        db.document(FirestorePaths.user(uid)).set(fields, SetOptions.merge()).await()
    }

    // CustomDays/{name}: workout templates

    fun observeTemplates(uid: String): Flow<List<TrainingDayDto>> =
        db.collection(FirestorePaths.templates(uid)).snapshots().map { query ->
            query.documents.mapNotNull { it.toTrainingDayOrNull() }
        }

    suspend fun setTemplate(uid: String, name: String, template: TrainingDayDto) {
        db.document(FirestorePaths.template(uid, name)).set(template).await()
    }

    suspend fun deleteTemplate(uid: String, name: String) {
        db.document(FirestorePaths.template(uid, name)).delete().await()
    }

    // TrainingDays/{yyyy-MM-dd}: workouts assigned to dates

    /** Null when the date has no document (a rest day). */
    suspend fun getTrainingDay(uid: String, date: LocalDate): TrainingDayDto? =
        db.document(FirestorePaths.trainingDay(uid, date)).get().await().toObject(TrainingDayDto::class.java)

    /** Documents for dates in [from, toExclusive), keyed by document id ("yyyy-MM-dd"), in one query. */
    fun observeTrainingDays(uid: String, from: LocalDate, toExclusive: LocalDate): Flow<Map<String, TrainingDayDto>> =
        db.collection(FirestorePaths.trainingDays(uid))
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), FirestorePaths.dateId(from))
            .whereLessThan(FieldPath.documentId(), FirestorePaths.dateId(toExclusive))
            .snapshots()
            .map { query ->
                query.documents.mapNotNull { document -> document.toTrainingDayOrNull()?.let { document.id to it } }.toMap()
            }

    /**
     * The same range as [observeTrainingDays], read once from the server only (fails offline), keyed by document id.
     */
    suspend fun getTrainingDaysFromServer(uid: String, from: LocalDate, toExclusive: LocalDate): Map<String, TrainingDayDto> =
        db.collection(FirestorePaths.trainingDays(uid))
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), FirestorePaths.dateId(from))
            .whereLessThan(FieldPath.documentId(), FirestorePaths.dateId(toExclusive))
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { document -> document.toTrainingDayOrNull()?.let { document.id to it } }
            .toMap()

    /**
     * Sets and deletes training-day documents in one batch: a null value deletes that date's document, anything
     * else replaces it. All or nothing.
     */
    suspend fun writeTrainingDays(uid: String, days: Map<LocalDate, TrainingDayDto?>) {
        val batch = db.batch()
        for ((date, day) in days) {
            val ref = db.document(FirestorePaths.trainingDay(uid, date))
            if (day == null) batch.delete(ref) else batch.set(ref, day)
        }
        batch.commit().await()
    }

    suspend fun setTrainingDay(uid: String, date: LocalDate, day: TrainingDayDto) {
        db.document(FirestorePaths.trainingDay(uid, date)).set(day).await()
    }

    suspend fun deleteTrainingDay(uid: String, date: LocalDate) {
        db.document(FirestorePaths.trainingDay(uid, date)).delete().await()
    }

    /** Fails if the date has no document. */
    suspend fun updateExercises(uid: String, date: LocalDate, exercises: List<ExerciseDto>) {
        db.document(FirestorePaths.trainingDay(uid, date)).update(EXERCISES_FIELD, exercises).await()
    }

    // MealDays/{yyyy-MM-dd}

    fun observeMealDay(uid: String, date: LocalDate): Flow<MealDayDto?> =
        db.document(FirestorePaths.mealDay(uid, date)).snapshots().map { it.toObject(MealDayDto::class.java) }

    /** Appends a meal, creating the day's document if it doesn't exist (the old app's `update` never did). */
    suspend fun addMeal(uid: String, date: LocalDate, meal: MealDto) {
        db.document(FirestorePaths.mealDay(uid, date))
            .set(mapOf(MEALS_FIELD to FieldValue.arrayUnion(meal)), SetOptions.merge())
            .await()
    }

    /** Skips a document that can't be read instead of failing the whole list (as the old app did). */
    private fun DocumentSnapshot.toTrainingDayOrNull(): TrainingDayDto? =
        try {
            toObject(TrainingDayDto::class.java)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Skipping unreadable document ${reference.path}", e)
            null
        }

    private companion object {
        const val TAG = "FirestoreDataSource"
        const val EXERCISES_FIELD = "exercises"
        const val MEALS_FIELD = "meals"
    }
}
