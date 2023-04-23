package com.myapp.lexicon.cloudstorage

import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.TEST_DB_NAME
import com.myapp.lexicon.createTestDB
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


const val TEST_ADS_ID = "Test-55555-44444-33333-22222-11111"

@RunWith(AndroidJUnit4ClassRunner::class)
class UploadDbWorkerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity { activity ->
            activity.createTestDB()
        }
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

            UploadDbWorker.uploadDbToCloud(activity, dbName = TEST_DB_NAME, TEST_ADS_ID, object : UploadDbWorker.Listener {
                override fun onSuccess(uri: Uri) {
                    val lastSegment = uri.lastPathSegment
                    val actualResult = lastSegment?.contains(TEST_DB_NAME)
                    Assert.assertEquals(true, actualResult)
                }
                override fun onFailure(error: String) {
                    Assert.assertTrue(error, false)
                }
                override fun onComplete() {
                    isRunning = false
                }

                override fun onCanceled(message: String) {
                    Assert.assertTrue(message, false)
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }

    }
}