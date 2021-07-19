package com.myapp.lexicon.database

import androidx.room.*
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface AppDao
{
    @Query("UPDATE Words SET dict_name = :newValue WHERE dict_name == :oldValue")
    fun updateColumnDictName(oldValue: String, newValue: String) : Single<Int>

    @Query("SELECT * FROM Words WHERE dict_name == :name AND _id >= :id AND count_repeat >= :repeat LIMIT :limit")
    fun getEntriesByDictName(name: String, id: Int = 1, repeat: Int = 1, limit: Int = 2) : Single<MutableList<Word>>

    @Query("SELECT * FROM Words WHERE _id IN(:id)")
    fun getEntriesById(id: List<Int>) : Single<MutableList<Word>>

    @Query("SELECT * FROM Words WHERE dict_name == :dict AND _id <> :id ORDER BY random() LIMIT 1")
    fun getRandomEntries(dict: String, id: Int) : Single<Word>

    @Query("SELECT * FROM Words WHERE dict_name == :dict AND translate like :like")
    fun getAllSimilarEntries(dict: String, like: String) : Single<MutableList<Word>>

    @Query("SELECT DISTINCT dict_name FROM Words")
    fun getDictList() : Single<MutableList<String>>

    @Update(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateEntries(list: List<Word>) : Single<Int>

    @Query("UPDATE Words SET count_repeat = :countRepeat WHERE _id >= :minId AND _id <= :maxId")
    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    @Insert
    fun insert(word: Word): Single<Long>

    @Insert(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<Word>): List<Long>

    @Query("SELECT count() FROM Words WHERE dict_name = :dict AND count_repeat <= 0 UNION SELECT count() FROM Words WHERE dict_name = :dict")
    fun getCounters(dict: String) : Single<List<Int>>

    @Query("SELECT count() FROM Words WHERE _id <= :id AND dict_name == :dict AND count_repeat > 0 UNION ALL SELECT count() FROM Words WHERE dict_name == :dict UNION ALL SELECT count() FROM Words WHERE dict_name == :dict AND count_repeat <= 0")
    fun getCounters(dict: String, id: Int) : Single<List<Int>>

    @Delete()
    fun delete(word: Word) : Single<Int>

    @Query("DELETE FROM Words WHERE dict_name == :dict")
    fun deleteEntriesByDictName(dict: String) : Single<Int>

}