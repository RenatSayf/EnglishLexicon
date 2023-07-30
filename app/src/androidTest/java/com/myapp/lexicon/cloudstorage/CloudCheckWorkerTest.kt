package com.myapp.lexicon.cloudstorage

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.createTestDB
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4ClassRunner::class)
class CloudCheckWorkerTest {

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
    fun doWork_Not_require_sync() {

        println("**************************** start doWork_NotRequireSync() test ***********************")
        var isRunning = true

        scenario.onActivity { activity ->

            val testDbName = activity.getString(R.string.test_db_name_1)
            val testToken = activity.getString(R.string.test_cloud_token)

            CloudCheckWorker.check(
                activity,
                testToken,
                testDbName,
                listener = object : CloudCheckWorker.Listener() {
                override fun onRequireUpSync(token: String) {
                    Assert.assertTrue(false)
                    isRunning = false
                }

                override fun onRequireDownSync(token: String) {
                    Assert.assertTrue(false)
                    isRunning = false
                }

                override fun onNotRequireSync() {
                    Assert.assertTrue(true)
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun doWork_Require_up_sync() {

        println("**************************** start doWork_NotRequireSync() test ***********************")
        var isRunning = true
        var file: File? = null

        scenario.onActivity { activity ->

            val testDbName = activity.getString(R.string.test_db_name_2)
            val testToken = activity.getString(R.string.test_cloud_token)

            file = activity.createTestDB(testDbName)

            CloudCheckWorker.check(
                activity,
                testToken,
                testDbName,
                listener = object : CloudCheckWorker.Listener() {
                override fun onRequireUpSync(token: String) {
                    Assert.assertTrue(true)
                    isRunning = false
                }

                override fun onRequireDownSync(token: String) {
                    Assert.assertTrue(false)
                    isRunning = false
                }

                override fun onNotRequireSync() {
                    Assert.assertTrue(false)
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
        file?.delete()
    }

}