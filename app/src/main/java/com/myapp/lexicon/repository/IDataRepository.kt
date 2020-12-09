package com.myapp.lexicon.repository

import com.myapp.lexicon.database.DataBaseEntry
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

interface IDataRepository
{
    fun getTableListFromDb() : Observable<MutableList<String>>

    fun getTableListFromSettings() : MutableList<String>

    fun getCurrentWordFromSettings() : DataBaseEntry

    fun saveCurrentWordTheSettings(entry: DataBaseEntry)

    fun getAllFromTable(tableName: String) : Single<MutableList<DataBaseEntry>>

    fun removeTableFromSettings(tableName: String)

    fun deleteTableFromDb(tableName: String) : Observable<Boolean>

    fun dropTableFromDb(tableName: String) : Single<Boolean>

    fun getRandomEntriesFromDb(tableName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>

    fun getEntriesAndCountersFromDb(tableName: String, rowId: Int, order: String, limit: Int) : Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
}