package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.AppDataBase



object AppRoomDbModule
{
    fun provideAppRoomDb(context: Context) : AppDao
    {
        return AppDataBase.getDbInstance(context).appDao()
    }
}