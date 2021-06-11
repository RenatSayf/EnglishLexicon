package com.myapp.lexicon.repository

import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.Word
import io.reactivex.Observable
import io.reactivex.Single

interface IDataRepository
{
    fun getTableListFromDb() : Observable<MutableList<String>>

    fun getDictListFromDb() : Single<MutableList<String>>

    fun getEntriesFromDbByDictName(dictName: String, id: Int = 1, limit: Int = 2) : Single<MutableList<Word>>

    fun getEntriesByIds(ids: List<Int>) : Single<MutableList<Word>>

    fun getRandomEntriesFromDB(dictName: String, id: Int) : Single<Word>

    fun getAllSimilarEntriesFromDB(dictName: String, like: String) : Single<MutableList<Word>>

    fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int) : Single<Int>

    fun insertEntry(word: Word): Single<Long>

    fun updateEntries(words: List<Word>) : Single<Int>

    fun deleteEntry(word: Word) : Single<Int>

    fun getTableListFromSettings() : MutableList<String>

    fun getCountersFromDb(dictName: String) : Single<List<Int>>

    fun getCurrentWordFromSettings() : DataBaseEntry

    fun saveCurrentWordTheSettings(entry: DataBaseEntry)

    fun getWordFromPref() : Word

    fun saveWordThePref(word: Word)

    fun goForward(words: List<Word>)

    fun getTestIntervalFromPref() : Int

    fun getOrderPlay() : Int

    fun saveOrderPlay(order: Int)

    fun saveWordsIdStringToPref(strIds: String)

    fun getWordsIdStringFromPref() : String

    fun getAllFromTable(tableName: String) : Single<MutableList<DataBaseEntry>>

    fun removeTableFromSettings(tableName: String)

    fun deleteTableFromDb(tableName: String) : Observable<Boolean>

    fun dropTableFromDb(tableName: String) : Single<Boolean>

    fun getRandomEntriesFromDb(tableName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>

    fun getEntriesAndCountersFromDb(tableName: String, rowId: Int, order: String, limit: Int) : Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>

    fun isSpeechEnable() : Boolean

    fun enableSpeech(isEnable: Boolean)

    fun isEngSpeech() : Boolean

    fun setEngSpeech(isSpeech: Boolean)

    fun isRusSpeech() : Boolean

    fun setRusSpeech(isSpeech: Boolean)
}