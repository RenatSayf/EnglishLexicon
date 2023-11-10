package com.myapp.lexicon.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    override suspend fun getRandomEntriesFromDB(dictName: String, id: Int): Deferred<Word>
    {
        return coroutineScope {
            async {
                db.getRandomEntries(dictName, id)
            }
        }
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

    override fun deleteEntriesByDictName(dictName: String): Single<Int> {
        return db.deleteEntriesByDictName(dictName)
    }

    override fun getCountersFromDb(dictName: String): Single<List<Int>>
    {
        return db.getCounters(dictName)
    }

    override fun getCountersFromDb(dictName: String, id: Int): Single<List<Int>>
    {
        return db.getCounters(dictName, id)
    }

    override fun goForward(words: List<Word>)
    {
        settings.goForward(words)
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

    override suspend fun getEntriesByDictNameAsync(
        dict: String,
        id: Long,
        repeat: Int,
        limit: Int
    ): Deferred<List<Word>> {
        return coroutineScope {
            async {
                db.getEntriesByDictName(dict, id, repeat, limit)
            }
        }
    }

    override suspend fun getFirstEntryAsync(): Deferred<Word> {
        return coroutineScope {
            async {
                db.getFirstEntry()
            }
        }
    }

    override suspend fun deleteEntriesByDictNameAsync(dicts: List<String>): Deferred<Int> {
        return coroutineScope {
            async {
                db.deleteEntriesByDictName(dicts)
            }
        }
    }

    override suspend fun getOrderedEntriesByDictNameAsync(
        dict: String,
        order: Int
    ): Deferred<List<Word>> {
        val orderStr = if (order > 0) {
            "english ASC"
        } else if (order < 0) {
            "english DESC"
        } else "random()"

        val query3 = "INSERT INTO PlayList SELECT * FROM Words WHERE dict_name = '$dict' AND count_repeat > 0 ORDER BY $orderStr"

        return coroutineScope {
            async {

                db.clearPlayList()
                db.runTimeQuery(SimpleSQLiteQuery(query3))
                val playList = db.getPlayList()
                playList.map { it.toWord() }
            }
        }
    }

}