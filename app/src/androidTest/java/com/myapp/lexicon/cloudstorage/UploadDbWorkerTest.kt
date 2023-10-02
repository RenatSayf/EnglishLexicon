package com.myapp.lexicon.cloudstorage

import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.createTestDB
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


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

            val testDbName = activity.getString(R.string.test_db_name_1)
            val testToken = activity.getString(R.string.test_cloud_token)
            UploadDbWorker.uploadDbToCloud(activity, dbName = testDbName, userId = testToken, object : UploadDbWorker.Listener {
                override fun onSuccess(uri: Uri) {
                    val lastSegment = uri.lastPathSegment
                    val actualResult = lastSegment?.contains(testDbName)?: false
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