package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DataRepositoryModule
{
    @Provides
    @Singleton
    fun provideDataRepository(db: AppDao, settings: AppSettings) : DataRepositoryImpl
    {
        return DataRepositoryImpl(db, settings)
    }
}