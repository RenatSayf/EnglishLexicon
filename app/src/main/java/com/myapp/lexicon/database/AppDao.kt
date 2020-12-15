package com.myapp.lexicon.database

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface AppDao
{
    @Query("UPDATE Words SET dict_name = :newValue WHERE dict_name == :oldValue")
    fun updateColumnDictName(oldValue: String, newValue: String) : Single<Int>

    @Query("SELECT name FROM sqlite_master WHERE type='table' \n" +
            "AND name <> 'android_metadata' \n" +
            "AND name <> 'com_myapp_lexicon_api_keys'\n" +
            "AND name <> 'sqlite_sequence'\n" +
            "AND name <> 'Words' \n" +
            "ORDER BY name")
    fun getTableList() : Observable<List<String>>

    @Query("SELECT * FROM Words WHERE dict_name == :name AND _id >= :id AND count_repeat <> 0 LIMIT :limit")
    fun getEntriesByDictName(name: String, id: Int = 1, limit: Int = 2) : Single<MutableList<Word>>

}