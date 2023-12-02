package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.repository.DataRepositoryImpl


object DataRepositoryModule
{
    fun provideDataRepository(db: AppDao) : DataRepositoryImpl
    {
        return DataRepositoryImpl(db)
    }
}