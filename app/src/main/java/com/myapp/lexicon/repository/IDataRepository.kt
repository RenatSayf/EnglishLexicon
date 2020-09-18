package com.myapp.lexicon.repository

import com.myapp.lexicon.database.DataBaseEntry
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

interface IDataRepository
{
    fun getTableListFromDb() : Observable<LinkedList<String>>

    fun getTableListFromSettings() : LinkedList<String>

    fun getAllFromTable(tableName: String) : Single<LinkedList<DataBaseEntry>>

    fun removeTableFromSettings(tableName: String)

    fun deleteTableFromDb(tableName: String) : Observable<Boolean>

    fun dropTableFromDb(tableName: String) : Single<Boolean>
}