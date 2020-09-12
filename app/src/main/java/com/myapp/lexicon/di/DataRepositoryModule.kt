package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
object DataRepositoryModule
{
    @Provides
    @Singleton
    fun provideDataRepository(appDB: AppDB, settings: AppSettings) : DataRepositoryImpl
    {
        return DataRepositoryImpl(appDB, settings)
    }
}