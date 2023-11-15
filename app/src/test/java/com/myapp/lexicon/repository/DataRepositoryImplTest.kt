package com.myapp.lexicon.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myapp.lexicon.database.AppDao
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31, manifest = Config.NONE)
class DataRepositoryImplTest {

    private lateinit var dao: AppDao
    private lateinit var db: AppDataBase
    private lateinit var repository: DataRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
        repository = DataRepositoryImpl(dao, AppSettings(context))

        runBlocking {
            repository.insertWordListAsync(
                listOf(
                    Word(dictName = "Test Set", english = "small", translate = "маленький", countRepeat = 1, _id = 0),
                    Word(dictName = "Test Set", english = "big", translate = "большой", countRepeat = 1, _id = 0),
                    Word(dictName = "Test Set", english = "fast", translate = "быстрый", countRepeat = 1, _id = 0)
                )
            ).await()
            delay(2000)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getOrderedEntriesByDictNameAsync_order_ASC() {

        runBlocking {
            val wordList = repository.getPlayListByDictNameAsync("Test Set", order = 0).await()
            val actualFirst = wordList.firstOrNull()?.english
            val actualLast = wordList.lastOrNull()?.english

            Assert.assertEquals("big", actualFirst)
            Assert.assertEquals("small", actualLast)
        }
    }

    @Test
    fun getOrderedEntriesByDictNameAsync_order_DESC() {

        runBlocking {
            val wordList = repository.getPlayListByDictNameAsync("Test Set", order = 1).await()
            val actualFirst = wordList.firstOrNull()?.english
            val actualLast = wordList.lastOrNull()?.english

            Assert.assertEquals("small", actualFirst)
            Assert.assertEquals("big", actualLast)
        }
    }

    @Test
    fun getOrderedEntriesByDictNameAsync_order_RANDOM() {

        runBlocking {
            val wordList = repository.getPlayListByDictNameAsync("Test Set", order = 2).await()
            val actualFirst = wordList.firstOrNull()?.english
            val actualLast = wordList.lastOrNull()?.english

            Assert.assertTrue(actualFirst == "big" || actualFirst == "fast" || actualFirst == "small")
            Assert.assertTrue(actualLast == "big" || actualLast == "fast" || actualLast == "small")
        }
    }

}