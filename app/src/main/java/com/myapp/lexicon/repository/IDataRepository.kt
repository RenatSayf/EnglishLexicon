package com.myapp.lexicon.repository

import io.reactivex.Observable
import java.util.*

interface IDataRepository
{
    fun getTableListFromDb() : Observable<LinkedList<String>>

    fun getTableListFromSettings() : LinkedList<String>
}