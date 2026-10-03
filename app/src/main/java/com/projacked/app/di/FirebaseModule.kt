package com.projacked.app.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.projacked.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Firebase SDK entry points. In debug builds (unless `projacked.useEmulator=false` is set in
 * local.properties) they're pointed at the Firebase Local Emulator Suite, so development never touches real
 * user data. Ports must match firebase.json.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val AUTH_EMULATOR_PORT = 9099
    private const val FIRESTORE_EMULATOR_PORT = 8080

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth.apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, AUTH_EMULATOR_PORT)
        }
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore.apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
            // Keep emulator data out of the on-disk cache, so it can never mix with cached live data.
            firestoreSettings = firestoreSettings { setLocalCacheSettings(memoryCacheSettings {}) }
        }
    }
}
