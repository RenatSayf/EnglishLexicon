package com.myapp.lexicon.repository

import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(private val appDB: AppDB, private val settings: AppSettings) : IDataRepository
{
    override fun getTableListFromDb(): Observable<LinkedList<String>>
    {
        return appDB.getTableListAsync()
    }

    override fun getTableListFromSettings(): LinkedList<String>
    {
        return settings.getPlayList(true)
    }
}