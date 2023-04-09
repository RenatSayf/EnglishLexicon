package com.myapp.lexicon.repository

import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.Single
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    private val db: AppDao,
    private var settings: AppSettings
) : IDataRepository
{
    override fun getDictListFromDb(): Single<MutableList<String>>
    {
        return db.getDictList()
    }

    override fun getEntriesFromDbByDictName(dictName: String, id: Int, repeat: Int, limit: Int): Single<MutableList<Word>>
    {
        return db.getEntriesByDictName(dictName, id, repeat, limit)
    }

    override fun getEntriesByIds(ids: List<Int>): Single<MutableList<Word>>
    {
        return db.getEntriesById(ids)
    }

    override fun getRandomEntriesFromDB(dictName: String, id: Int): Single<Word>
    {
        return db.getRandomEntries(dictName, id)
    }

    override fun getAllSimilarEntriesFromDB(dictName: String, like: String): Single<MutableList<Word>>
    {
        return db.getAllSimilarEntries(dictName, like)
    }

    override fun updateCountRepeat(countRepeat: Int, minId: Int, maxId: Int): Single<Int>
    {
        return db.updateCountRepeat(countRepeat, minId, maxId)
    }

    override fun insertEntry(word: Word): Single<Long>
    {
        return db.insert(word)
    }

    override fun insertEntry(list: List<Word>): List<Long>
    {
        return db.insert(list)
    }

    override fun updateEntries(words: List<Word>): Single<Int>
    {
        return db.updateEntries(words)
    }

    override fun deleteEntry(word: Word): Single<Int>
    {
        return db.delete(word)
    }

    override fun deleteEntriesByDictName(dictName: String): Single<Int>
    {
        val result = db.deleteEntriesByDictName(dictName)
        return result
    }

    override fun getCountersFromDb(dictName: String): Single<List<Int>>
    {
        return db.getCounters(dictName)
    }

    override fun getCountersFromDb(dictName: String, id: Int): Single<List<Int>>
    {
        return db.getCounters(dictName, id)
    }

    override fun getCurrentWordFromSettings(): DataBaseEntry
    {
        return settings.currentWord
    }

    override fun saveCurrentWordTheSettings(entry: DataBaseEntry)
    {
        settings.saveCurrentWord(entry)
    }

    override fun getWordFromPref(): Word
    {
        return settings.wordFromPref
    }

    override fun saveWordThePref(word: Word)
    {
        settings.saveWordThePref(word)
    }

    override fun goForward(words: List<Word>)
    {
        settings.goForward(words)
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

    override fun saveWordsIdStringToPref(strIds: String)
    {
        settings.saveWordsIdAsString(strIds)
    }

    override fun getWordsIdStringFromPref(): String
    {
        return settings.wordsIdsAsString
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