package com.myapp.lexicon.repository

import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.testing.TestActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class DataRepositoryImplTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var subscribe: Disposable
    private lateinit var repository: DataRepositoryImpl

    @Before
    fun setUp() {

        scenario = rule.scenario
        scenario.onActivity {

            val testDbName = it.getString(R.string.test_db_name_1)
            val dataBase = Room.databaseBuilder(it, AppDataBase::class.java, testDbName)
                .createFromAsset("databases/$testDbName")
                .allowMainThreadQueries()
                .build().appDao()

            repository = DataRepositoryImpl(dataBase, AppSettings(it))
        }
    }

    @After
    fun tearDown() {
        subscribe.dispose()
        scenario.close()
    }

    @Test
    fun deleteEntriesByDictName_Success() {

        var isRunning = true

        scenario.onActivity { activity ->

            activity.lifecycleScope.launch {
                delay(120000)
                Assert.assertTrue("********** Test timeout ***********", false)
                isRunning = false
            }

            val dictName = "Тестовый словарь"
            subscribe = repository.insertWordAsync(
                Word(0, dictName, "Asdfghjk", "Фывапрод", 1)
            ).subscribe({ id ->
                Assert.assertTrue("******* id = $id *************", id > 0)
            }, {
                Assert.assertTrue(it.message, false)
                isRunning = false
            })

            Thread.sleep(3000)

            subscribe = repository.deleteEntriesByDictName(dictName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Assert.assertTrue(it > 0)
                    isRunning = false
                }, {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun deleteEntriesByDictName_NoExist() {
        var isRunning = true

        scenario.onActivity { activity ->

            activity.lifecycleScope.launch {
                delay(120000)
                Assert.assertTrue("********** Test timeout ***********", false)
                isRunning = false
            }

            subscribe = repository.deleteEntriesByDictName("Xxxxxx xxxx")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Assert.assertTrue(it == 0)
                    isRunning = false
                }, {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

}

