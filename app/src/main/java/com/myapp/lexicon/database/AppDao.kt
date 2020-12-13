package com.myapp.lexicon.database

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface AppDao
{
    @Query("UPDATE Words SET dict_name = :newValue WHERE dict_name == :oldValue")
    fun updateColumnDictName(oldValue: String, newValue: String)
}