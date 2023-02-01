package com.ssafy.campinity.di

import com.ssafy.campinity.domain.repository.AuthRepository
import com.ssafy.campinity.domain.repository.CollectionRepository
import com.ssafy.campinity.domain.repository.CurationRepository
import com.ssafy.campinity.domain.repository.UserRepository
import com.ssafy.campinity.domain.usecase.auth.LoginUseCase
import com.ssafy.campinity.domain.usecase.collection.GetCollectionDetailUseCase
import com.ssafy.campinity.domain.usecase.collection.GetCollectionsUseCase
import com.ssafy.campinity.domain.usecase.curation.GetCurationDetailUseCase
import com.ssafy.campinity.domain.usecase.curation.GetCurationsUseCase
import com.ssafy.campinity.domain.usecase.user.CheckDuplicationUseCase
import com.ssafy.campinity.domain.usecase.user.EditUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

    @Singleton
    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase =
        LoginUseCase(authRepository)

    @Singleton
    @Provides
    fun provideEditUserUseCase(userRepository: UserRepository): EditUserUseCase =
        EditUserUseCase(userRepository)

    @Singleton
    @Provides
    fun provideCheckDuplicationUseCase(userRepository: UserRepository): CheckDuplicationUseCase =
        CheckDuplicationUseCase(userRepository)

    @Singleton
    @Provides
    fun provideGetCollectionsUseCase(collectionRepository: CollectionRepository): GetCollectionsUseCase =
        GetCollectionsUseCase(collectionRepository)

    @Singleton
    @Provides
    fun provideGetCollectionDetailUseCase(collectionRepository: CollectionRepository): GetCollectionDetailUseCase =
        GetCollectionDetailUseCase(collectionRepository)

    @Singleton
    @Provides
    fun provideGetCurationsUseCase(curationRepository: CurationRepository): GetCurationsUseCase =
        GetCurationsUseCase(curationRepository)

    @Singleton
    @Provides
    fun provideGetCurationUseCase(curationRepository: CurationRepository): GetCurationDetailUseCase =
        GetCurationDetailUseCase(curationRepository)
}