package com.myapp.lexicon.repository

import com.myapp.lexicon.database.*
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(private val appDB: AppDB,
                                             private val db: AppDao,
                                             private var settings: AppSettings) : IDataRepository
{

    override fun getTableListFromDb(): Observable<MutableList<String>>
    {
        return appDB.getTableListAsync()
    }

    override fun getDictListFromDb(): Single<MutableList<String>>
    {
        return db.getDictList()
    }

    override fun getEntriesFromDbByDictName(dictName: String, id: Int, limit: Int): Single<MutableList<Word>>
    {
        return db.getEntriesByDictName(dictName, id, limit)
    }

    override fun updateDbEntries(list: List<Word>): Single<Int>
    {
        return db.updateCountRepeat(0, 1, Int.MAX_VALUE)
    }

    override fun insertEntry(word: Word): Single<Long>
    {
        return db.insert(word)
    }

    override fun getTableListFromSettings(): MutableList<String>
    {
        return settings.getPlayList(true)
    }

    override fun getCurrentWordFromSettings(): DataBaseEntry
    {
        return settings.currentWord
    }

    override fun saveCurrentWordTheSettings(entry: DataBaseEntry)
    {
        settings.saveCurrentWord(entry);
    }

    override fun getWordFromPref(): Word
    {
        return settings.wordFromPref
    }

    override fun saveWordThePref(word: Word)
    {
        settings.saveWordThePref(word)
    }

    override fun getTestIntervalFromPref(): Int
    {
        return settings.wordsInterval
    }

    override fun getOrderPlay(): Int
    {
        return settings.orderPlay
    }

    override fun saveOrderPlay(order: Int)
    {
        settings.orderPlay = order
    }

    override fun getAllFromTable(tableName: String): Single<MutableList<DataBaseEntry>>
    {
        return appDB.getAllFromTableAsync(tableName)
    }

    override fun removeTableFromSettings(tableName: String)
    {
        return settings.removeItemFromPlayList(tableName)
    }

    override fun deleteTableFromDb(tableName: String): Observable<Boolean>
    {
        return appDB.deleteTableFromDbAsync(tableName)
    }

    override fun dropTableFromDb(tableName: String): Single<Boolean>
    {
        return appDB.dropTableFromDb(tableName)
    }

    override fun getRandomEntriesFromDb(tableName: String, rowId: Int): Single<MutableList<DataBaseEntry>>
    {
        return appDB.getRandomEntriesFromDbAsync(tableName, rowId)
    }

    override fun getEntriesAndCountersFromDb(tableName: String, rowId: Int, order: String, limit: Int): Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        return appDB.getEntriesAndCountersAsync(tableName, rowId, order, limit)
    }

    override fun isSpeechEnable(): Boolean
    {
        return settings.isSpeech
    }

    override fun enableSpeech(isEnable: Boolean)
    {
        settings.enableSpeech(isEnable)
    }

    override fun isEngSpeech(): Boolean
    {
        return settings.isEngSpeech
    }

    override fun setEngSpeech(isSpeech: Boolean)
    {
        settings.isEngSpeech = isSpeech
    }

    override fun isRusSpeech(): Boolean
    {
        return settings.isRusSpeech
    }

    override fun setRusSpeech(isSpeech: Boolean)
    {
        settings.isRusSpeech = isSpeech
    }
}