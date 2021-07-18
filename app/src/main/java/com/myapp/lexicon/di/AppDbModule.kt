package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppDbModule
{
    @Provides
    @Singleton
    fun provideAppDB(dbHelper: DatabaseHelper, dbRoom: AppDao) : AppDB
    {
        return AppDB(dbHelper, dbRoom)
    }
}