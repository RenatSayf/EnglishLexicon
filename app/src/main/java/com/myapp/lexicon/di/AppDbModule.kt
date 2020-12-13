package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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

    @Provides
    @Singleton
    fun provideRoomDb(@ApplicationContext context: Context) : AppDataBase
    {
        return AppDataBase.getInstance(context)
    }
}