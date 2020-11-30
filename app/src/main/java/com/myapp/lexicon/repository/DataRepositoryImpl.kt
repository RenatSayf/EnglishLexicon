package com.myapp.lexicon.repository

import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.Observable
import io.reactivex.Single
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

    override fun getAllFromTable(tableName: String): Single<LinkedList<DataBaseEntry>>
    {
        return appDB.getAllFromTableAsync(tableName)
    }

    override fun removeTableFromSettings(tableName: String)
    {
        return settings.removeItemFromPlayList(tableName)
    }

    override fun deleteTableFromDb(tableName: String): Observable<Boolean>
    {
        return appDB.deleteTableFromDbAsync(tableName)
    }

    override fun dropTableFromDb(tableName: String): Single<Boolean>
    {
        return appDB.dropTableFromDb(tableName)
    }

    override fun getEntriesAndCountersFromDb(tableName: String, rowId: Int, order: String): Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        return appDB.getEntriesAndCountersAsync(tableName, rowId, order)
    }
}