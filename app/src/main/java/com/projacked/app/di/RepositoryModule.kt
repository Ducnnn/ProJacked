package com.projacked.app.di

import com.projacked.app.data.repository.AuthRepositoryImpl
import com.projacked.app.data.repository.NutritionRepositoryImpl
import com.projacked.app.data.repository.ProfileRepositoryImpl
import com.projacked.app.data.repository.TrainingRepositoryImpl
import com.projacked.app.domain.repository.AuthRepository
import com.projacked.app.domain.repository.NutritionRepository
import com.projacked.app.domain.repository.ProfileRepository
import com.projacked.app.domain.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds each domain repository interface to its Firebase-backed implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(impl: TrainingRepositoryImpl): TrainingRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository
}
