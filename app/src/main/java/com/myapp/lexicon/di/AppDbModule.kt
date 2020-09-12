package com.myapp.lexicon.di

import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppDbModule
{
    @Provides
    @Singleton
    fun provideAppDB(dbHelper: DatabaseHelper) : AppDB
    {
        return AppDB(dbHelper)
    }
}