package com.myapp.lexicon.repository

import com.myapp.lexicon.models.Word
import io.reactivex.Single
import kotlinx.coroutines.Deferred

interface IDataRepository
{
    fun getDictListFromDb() : Single<MutableList<String>>

    fun getEntriesFromDbByDictName(dictName: String, id: Int = 1, repeat: Int, limit: Int = 2) : Single<MutableList<Word>>

    fun getEntriesByIds(ids: List<Int>) : Single<MutableList<Word>>

    fun getRandomEntriesFromDB(dictName: String, id: Int) : Single<Word>

    fun getAllSimilarEntriesFromDB(dictName: String, like: String) : Single<MutableList<Word>>

    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    fun insertEntry(word: Word): Single<Long>

    fun insertEntry(list: List<Word>): List<Long>

    fun updateEntries(words: List<Word>) : Single<Int>

    fun deleteEntry(word: Word) : Single<Int>

    fun deleteEntriesByDictName(dictName: String) : Single<Int>

    fun getCountersFromDb(dictName: String) : Single<List<Int>>

    fun getCountersFromDb(dictName: String, id: Int) : Single<List<Int>>

    fun goForward(words: List<Word>)

    fun getOrderPlay() : Int

    fun saveOrderPlay(order: Int)

    fun saveWordsIdStringToPref(strIds: String)

    fun getWordsIdStringFromPref() : String

    fun isSpeechEnable() : Boolean

    fun enableSpeech(isEnable: Boolean)

    fun isEngSpeech() : Boolean

    fun setEngSpeech(isSpeech: Boolean)

    fun isRusSpeech() : Boolean

    fun setRusSpeech(isSpeech: Boolean)

    suspend fun getEntriesByDictNameAsync(dict: String, id: Long, repeat: Int = 1, limit: Int = 2): Deferred<List<Word>>

    suspend fun getFirstEntryAsync(): Deferred<Word>

    suspend fun deleteEntriesByDictNameAsync(dicts: List<String>): Deferred<Int>
}