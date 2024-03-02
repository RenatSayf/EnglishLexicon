package com.myapp.lexicon.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.myapp.lexicon.common.OrderBy
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.database.models.Counters
import com.myapp.lexicon.database.models.WordToPlay
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.video.models.query.HistoryQuery
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.jvm.Throws

class DataRepositoryImpl(
    private val db: AppDao
) : IDataRepository
{
    override fun getDictListFromDb(): Single<MutableList<String>>
    {
        return db.getDictList()
    }

    override fun getEntriesFromDbByDictName(dictName: String, id: Int, repeat: Int, orderBy: OrderBy, limit: Int): Single<MutableList<Word>>
    {
        val query = "SELECT * FROM Words WHERE dict_name == '$dictName' AND _id >= $id AND count_repeat >= $repeat ORDER BY ${orderBy.value} LIMIT $limit"
        return db.getOrderedWordsByDictName(SimpleSQLiteQuery(query))
    }

    override fun getEntriesByIds(ids: List<Int>): Single<MutableList<Word>>
    {
        return db.getEntriesById(ids)
    }

    override suspend fun getRandomEntriesFromDB(dictName: String, id: Int): Deferred<Word>
    {
        return coroutineScope {
            async {
                val wordToPlay = db.getRandomEntries(dictName, id)
                wordToPlay.toWord()
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

    override fun insertWordAsync(word: Word): Single<Long>
    {
        return db.insert(word)
    }

    override suspend fun insertWordListAsync(list: List<Word>): Deferred<List<Long>>
    {
        return coroutineScope {
            async {
                db.insert(list)
            }
        }
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

    override suspend fun getEntriesByDictNameAsync(
        dict: String,
        id: Long,
        repeat: Int,
        limit: Int
    ): Deferred<List<Word>> {
        return coroutineScope {
            async {
                val playList = db.getEntriesByDictName(dict, id, repeat, limit)
                playList.map { it.toWord() }
            }
        }
    }
    override suspend fun getFirstEntryAsync(): Deferred<Word?> {
        return coroutineScope {
            async {
                try {
                    db.getFirstEntry()
                } catch (e: Exception) {
                    e.throwIfDebug()
                    null
                }
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

    override suspend fun getPlayListByDictNameAsync(
        dict: String,
        order: Int
    ): Deferred<List<Word>> {
        val orderStr = when (order) {
            0 -> {
                OrderBy.ASC.value
            }
            1 -> {
                OrderBy.DESC.value
            }
            else -> OrderBy.RANDOM.value
        }

        val query = "INSERT OR replace INTO PlayList SELECT * FROM Words WHERE dict_name = '$dict' AND count_repeat > 0 ORDER BY $orderStr"

        return coroutineScope {
            async {
                db.clearPlayList()
                AppDataBase.execVacuum()
                db.runTimeQuery(SimpleSQLiteQuery(query))
                val playList = db.getPlayList()
                playList.map { it.toWord() }
            }
        }
    }

    override suspend fun getDictNameFromPlayListAsync(): Deferred<List<String>> {
        return coroutineScope {
            async {
                db.getDictNameFromPlayList()
            }
        }
    }

    override suspend fun getPlayListAsync(): Deferred<List<WordToPlay>> {
        return coroutineScope {
            async {
                db.getPlayList()
            }
        }
    }

    override suspend fun getWordPairFromPlayListAsync(id: Int): Deferred<List<Word>> {
        return coroutineScope {
            async {
                val wordPair = db.getWordPairFromPlayList(id)
                wordPair.map { it.toWord() }
            }
        }
    }

    override suspend fun getFirstFromPlayListAsync(): Deferred<List<Word>> {
        return coroutineScope {
            async {
                val first = db.getFirstFromPlayList()
                first.map { it.toWord() }
            }
        }
    }

    override suspend fun getCountersByIdAsync(id: Int): Deferred<List<Counters>> {
        return coroutineScope {
            async {
                db.getCountersById(id)
            }
        }
    }

    override suspend fun addVideoToHistory(item: HistoryQuery): Deferred<Result<Long>> {
        return coroutineScope {
            async {
                runCatching { db.insertInToHistory(item) }
            }
        }
    }

    override suspend fun getVideoHistory(): Deferred<Result<List<HistoryQuery>>> {
        return coroutineScope {
            async {
                runCatching { db.getAllFromHistory() }
            }
        }
    }

    override suspend fun getLatestVideoFromHistory(): Deferred<Result<HistoryQuery?>> {
        return coroutineScope {
            async {
                runCatching { db.getLatestEntryFromHistory() }
            }
        }
    }

    override fun dbClose() {
        AppDataBase.dbClose()
    }

    override fun reInitDataBase() {

    }


}