package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object AppRoomDbModule
{
    @Provides
    @Singleton
    fun provideAppRoomDb(@ApplicationContext context: Context) : AppDao
    {
        return AppDataBase.buildDataBase(context).appDao()
    }
}