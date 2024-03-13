package com.myapp.lexicon.database

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.myapp.lexicon.database.models.Counters
import com.myapp.lexicon.database.models.WordToPlay
import com.myapp.lexicon.models.Word
import io.reactivex.Single

@Dao
interface AppDao
{
    @Query("UPDATE Words SET dict_name = :newValue WHERE dict_name == :oldValue")
    fun updateColumnDictName(oldValue: String, newValue: String) : Single<Int>

    @RawQuery
    fun getOrderedWordsByDictName(query: SimpleSQLiteQuery): Single<MutableList<Word>>

    @Query("SELECT * FROM Words WHERE _id IN(:id)")
    fun getEntriesById(id: List<Int>) : Single<MutableList<Word>>

    @Query("SELECT * FROM PlayList WHERE dict_name == :dict AND _id <> :id ORDER BY random() LIMIT 1")
    fun getRandomEntries(dict: String, id: Int) : WordToPlay?

    @Query("SELECT * FROM Words WHERE dict_name == :dict AND english like :like")
    fun getAllSimilarEntries(dict: String, like: String) : Single<MutableList<Word>>

    @Query("SELECT DISTINCT dict_name FROM Words")
    fun getDictList() : Single<MutableList<String>>

    @Update(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateEntries(list: List<Word>) : Single<Int>

    @Query("UPDATE Words SET count_repeat = :countRepeat WHERE _id >= :minId AND _id <= :maxId")
    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    @Insert(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: Word): Single<Long>

    @Insert(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<Word>): List<Long>

    @Query("SELECT count() FROM Words WHERE dict_name = :dict AND count_repeat <= 0 UNION SELECT count() FROM Words WHERE dict_name = :dict")
    fun getCounters(dict: String) : Single<List<Int>>

    @Query("SELECT count() FROM Words WHERE _id <= :id AND dict_name == :dict AND count_repeat > 0 UNION ALL SELECT count() FROM Words WHERE dict_name == :dict UNION ALL SELECT count() FROM Words WHERE dict_name == :dict AND count_repeat <= 0")
    fun getCounters(dict: String, id: Int) : Single<List<Int>>

    @Delete(entity = Word::class)
    fun delete(word: Word) : Single<Int>

    @Query("DELETE FROM Words WHERE dict_name == :dict")
    fun deleteEntriesByDictName(dict: String) : Single<Int>

    @Query("SELECT * FROM PlayList WHERE dict_name == :dict AND _id >= :id AND count_repeat >= :repeat LIMIT :limit")
    suspend fun getEntriesByDictName(dict: String, id: Long, repeat: Int, limit: Int): List<WordToPlay>

    @Query("SELECT * FROM Words WHERE _id >= 1 LIMIT 1")
    suspend fun getFirstEntry(): Word

    @Query("DELETE FROM Words WHERE dict_name IN(:dicts)")
    suspend fun deleteEntriesByDictName(dicts: List<String>): Int

    @Query("SELECT dict_name FROM PlayList LIMIT 1")
    suspend fun getDictNameFromPlayList(): List<String>

    @Query("DELETE FROM PlayList")
    suspend fun clearPlayList(): Int

    @Query("SELECT * FROM PlayList")
    suspend fun getPlayList(): List<WordToPlay>

    @RawQuery
    suspend fun runTimeQuery(query: SimpleSQLiteQuery): List<Any>

    @Query("SELECT * FROM PlayList WHERE ROWID >= (SELECT ROWID FROM PlayList WHERE _id = :id) ORDER BY ROWID ASC LIMIT 2")
    suspend fun getWordPairFromPlayList(id: Int): List<WordToPlay>

    @Query("SELECT * FROM PlayList WHERE ROWID == 1")
    suspend fun getFirstFromPlayList(): List<WordToPlay>

    @Query("""SELECT ROWID AS row_num,
(SELECT count() FROM Words WHERE dict_name = (SELECT dict_name FROM PlayList LIMIT 1)) AS total_count,
(SELECT count() FROM Words WHERE dict_name = (SELECT dict_name FROM PlayList LIMIT 1) AND count_repeat <> 1) AS unused
 FROM PlayList WHERE _id = :id;""")
    suspend fun getCountersById(id: Int): List<Counters>

    // Such a request does not work correctly, for some reason
    @Query("INSERT OR replace INTO PlayList SELECT * FROM Words WHERE dict_name = :dict AND count_repeat > 0 ORDER BY :orderStr")
    suspend fun updatePlayListTable(dict: String, orderStr: String): Long

}

