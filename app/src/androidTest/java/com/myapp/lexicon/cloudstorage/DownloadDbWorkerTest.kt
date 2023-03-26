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

        val remoteRef =
            "https://firebasestorage.googleapis.com/v0/b/lexicon-b5d1a.appspot.com/o/users%2F75b55127-6d92-4339-b230-c3c6beed67b3%2Flexicon_DB.db?alt=media&token=87364069-8833-4a68-aaeb-d56cb0b16e5c"

        scenario.onActivity { activity ->

            DownloadDbWorker.downloadDbFromCloud(activity, remoteRef, object : DownloadDbWorker.Listener {
                override fun onSuccess(bytes: ByteArray) {
                    Assert.assertTrue(bytes.isNotEmpty())
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