package com.myapp.lexicon.repository

import com.myapp.lexicon.common.OrderBy
import com.myapp.lexicon.database.models.Counters
import com.myapp.lexicon.database.models.WordToPlay
import com.myapp.lexicon.models.Word
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import kotlin.jvm.Throws

interface IDataRepository
{
    fun getDictListFromDb() : Single<MutableList<String>>

    fun getEntriesFromDbByDictName(dictName: String, id: Int = 1, repeat: Int, orderBy: OrderBy = OrderBy.ASC, limit: Int = 2) : Single<MutableList<Word>>

    fun getEntriesByIds(ids: List<Int>) : Single<MutableList<Word>>

    suspend fun getRandomEntriesFromDB(dictName: String, id: Int) : Deferred<Word?>

    fun getAllSimilarEntriesFromDB(dictName: String, like: String) : Single<MutableList<Word>>

    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    fun insertWordAsync(word: Word): Single<Long>

    suspend fun insertWordListAsync(list: List<Word>): Deferred<List<Long>>

    fun updateEntries(words: List<Word>) : Single<Int>

    fun deleteEntry(word: Word) : Single<Int>

    fun deleteEntriesByDictName(dictName: String) : Single<Int>

    fun getCountersFromDb(dictName: String) : Single<List<Int>>

    fun getCountersFromDb(dictName: String, id: Int) : Single<List<Int>>

    suspend fun getEntriesByDictNameAsync(dict: String, id: Long, repeat: Int = 1, limit: Int = 2): Deferred<List<Word>>

    suspend fun getFirstEntryAsync(): Deferred<Word?>

    suspend fun deleteEntriesByDictNameAsync(dicts: List<String>): Deferred<Int>

    /**
     * @param dict - dict name
     * @param order 1 -> order ASC, -1 -> order DESC, 0 -> random()
     */
    suspend fun getPlayListByDictNameAsync(dict: String, order: Int = 1): Deferred<List<Word>>

    suspend fun getDictNameFromPlayListAsync(): Deferred<List<String>>

    suspend fun getPlayListAsync(): Deferred<List<WordToPlay>>

    suspend fun getWordPairFromPlayListAsync(id: Int): Deferred<List<Word>>

    suspend fun getFirstFromPlayListAsync(): Deferred<List<Word>>

    suspend fun getCountersByIdAsync(id: Int): Deferred<List<Counters>>

    fun dbClose()

    fun reInitDataBase()

}
