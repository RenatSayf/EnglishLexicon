package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext


@InstallIn(ApplicationComponent::class)
@Module
object AppSettingsModule
{
    @Provides
    fun provideAppSettings(@ApplicationContext context: Context) : AppSettings
    {
        return AppSettings(context)
    }
}