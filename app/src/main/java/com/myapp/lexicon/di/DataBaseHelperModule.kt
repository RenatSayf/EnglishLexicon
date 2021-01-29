package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataBaseHelperModule
{
    @Provides
    @Singleton
    fun provideDataBaseHelper(@ApplicationContext context: Context) : DatabaseHelper
    {
        return DatabaseHelper(context)
    }
}