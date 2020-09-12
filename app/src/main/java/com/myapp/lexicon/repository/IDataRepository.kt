package com.myapp.lexicon.repository

import com.myapp.lexicon.database.DataBaseEntry
import io.reactivex.Observable
import java.util.*

interface IDataRepository
{
    fun getTableListFromDb() : Observable<LinkedList<String>>

    fun getTableListFromSettings() : LinkedList<String>

    fun getAllFromTable(tableName: String) : Observable<LinkedList<DataBaseEntry>>
}