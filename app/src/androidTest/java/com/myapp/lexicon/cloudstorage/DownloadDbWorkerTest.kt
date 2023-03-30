package com.myapp.lexicon.cloudstorage

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class DownloadDbWorkerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

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

            DownloadDbWorker.downloadDbFromCloud(activity, TEST_DB_NAME, TEST_ADS_ID, object : DownloadDbWorker.Listener {
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