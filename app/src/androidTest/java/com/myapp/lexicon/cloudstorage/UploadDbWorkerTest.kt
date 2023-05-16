package com.myapp.lexicon.cloudstorage

import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.createTestDB
import com.myapp.lexicon.models.LaunchMode
import com.myapp.lexicon.settings.setCloudSetting
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.getCRC32CheckSum


@RunWith(AndroidJUnit4ClassRunner::class)
class UploadDbWorkerTest {

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
        scenario.onActivity { activity ->
            activity.createTestDB()
            activity.setCloudSetting(activity.getString(R.string.test_cloud_token))
        }
    }

    @After
    fun tearDown() {
        scenario.onActivity { activity ->
            activity.setCloudSetting(null)
        }
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
                    Assert.assertTrue(true)
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