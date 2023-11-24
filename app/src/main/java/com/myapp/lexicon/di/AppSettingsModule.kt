package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.settings.AppSettings


object AppSettingsModule
{
    fun provideAppSettings(context: Context) : AppSettings
    {
        return AppSettings(context)
    }
}