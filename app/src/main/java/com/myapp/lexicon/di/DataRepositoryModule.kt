package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.AppSettings


object DataRepositoryModule
{
    fun provideDataRepository(db: AppDao, settings: AppSettings) : DataRepositoryImpl
    {
        return DataRepositoryImpl(db, settings)
    }
}