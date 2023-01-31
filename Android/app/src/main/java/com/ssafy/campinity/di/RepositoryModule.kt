package com.ssafy.campinity.di

import com.ssafy.campinity.data.remote.datasource.auth.AuthRemoteDataSourceImpl
import com.ssafy.campinity.data.remote.datasource.collection.CollectionRemoteDataSourceImpl
import com.ssafy.campinity.data.remote.datasource.search.SearchRemoteDataSourceImpl
import com.ssafy.campinity.data.remote.datasource.user.UserRemoteDataSourceImpl
import com.ssafy.campinity.data.remote.repository.AuthRepositoryImpl
import com.ssafy.campinity.data.remote.repository.CollectionRepositoryImpl
import com.ssafy.campinity.data.remote.repository.SearchRepositoryImpl
import com.ssafy.campinity.data.remote.repository.UserRepositoryImpl
import com.ssafy.campinity.domain.repository.AuthRepository
import com.ssafy.campinity.domain.repository.CollectionRepository
import com.ssafy.campinity.domain.repository.SearchRepository
import com.ssafy.campinity.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRemoteDataSourceImpl: AuthRemoteDataSourceImpl
    ): AuthRepository = AuthRepositoryImpl(authRemoteDataSourceImpl)

    @Provides
    @Singleton
    fun provideUserRepository(
        userRemoteDataSourceImpl: UserRemoteDataSourceImpl
    ): UserRepository = UserRepositoryImpl(userRemoteDataSourceImpl)

    @Provides
    @Singleton
    fun provideCollectionRepository(
        collectionRemoteDataSourceImpl: CollectionRemoteDataSourceImpl
    ): CollectionRepository = CollectionRepositoryImpl(collectionRemoteDataSourceImpl)

    @Provides
    @Singleton
    fun provideSearchRepository(
        searchRemoteDataSourceImpl: SearchRemoteDataSourceImpl
    ): SearchRepository = SearchRepositoryImpl(searchRemoteDataSourceImpl)
}
