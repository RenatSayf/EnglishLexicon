package com.myapp.lexicon.database

import androidx.room.*
import io.reactivex.Observable
import io.reactivex.Single
import java.sql.RowId

@Dao
interface AppDao
{
    @Query("UPDATE Words SET dict_name = :newValue WHERE dict_name == :oldValue")
    fun updateColumnDictName(oldValue: String, newValue: String) : Single<Int>

    @Query("SELECT * FROM Words WHERE dict_name == :name AND _id >= :id AND count_repeat <> 0 LIMIT :limit")
    fun getEntriesByDictName(name: String, id: Int = 1, limit: Int = 2) : Single<MutableList<Word>>

    @Query("SELECT * FROM Words WHERE dict_name == :dict AND _id <> :id ORDER BY random() LIMIT 1")
    fun getRandomEntries(dict: String, id: Int) : Single<Word>

    @Query("SELECT DISTINCT dict_name FROM Words")
    fun getDictList() : Single<MutableList<String>>

    @Update(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateEntries(list: List<Word>) : Single<Int>

    @Query("UPDATE Words SET count_repeat = :countRepeat WHERE _id >= :minId AND _id <= :maxId")
    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    @Insert
    fun insert(word: Word): Single<Long>

    @Query("SELECT count() FROM Words WHERE dict_name = :dict AND count_repeat == 0 UNION SELECT count() FROM Words WHERE dict_name = :dict")
    fun getCounters(dict: String) : Single<List<Int>>

    @Delete()
    fun delete(word: Word) : Single<Int>

}