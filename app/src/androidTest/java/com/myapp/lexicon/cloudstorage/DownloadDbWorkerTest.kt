package com.myapp.lexicon.cloudstorage

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.TEST_DB_NAME
import com.myapp.lexicon.helpers.getCRC32CheckSum
import com.myapp.lexicon.models.LaunchMode
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class DownloadDbWorkerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    init {
        if (BuildConfig.PURCHASE_MODE != LaunchMode.TEST.name) {
            val message = "******* BuildConfig.PURCHASE_MODE must be TEST *************"
            throw Exception(message)
        }
    }

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun doWork() {

        println("**************************** start doWork() test ***********************")
        var isRunning = true

        scenario.onActivity { activity ->

            val userId = TEST_ADS_ID.getCRC32CheckSum().toString()
            DownloadDbWorker.downloadDbFromCloud(activity, TEST_DB_NAME, userId, object : DownloadDbWorker.Listener {
                override fun onSuccess(bytes: ByteArray) {
                    Assert.assertTrue(bytes.isNotEmpty())
                    val dbName = activity.databaseList().first {
                        it == TEST_DB_NAME
                    }
                    val databaseFile = activity.getDatabasePath(dbName)
                    val localBytes = databaseFile.readBytes()
                    val result = localBytes.contentEquals(bytes)
                    Assert.assertEquals(true, result)
                }

                override fun onFailure(error: String) {
                    Assert.assertTrue(error, false)
                }

                override fun onComplete() {
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}