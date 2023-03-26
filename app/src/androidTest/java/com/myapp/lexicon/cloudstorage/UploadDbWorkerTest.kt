package com.myapp.lexicon.cloudstorage

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.ads.getAdvertisingID
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

            activity.getAdvertisingID( onSuccess = { id ->

                UploadDbWorker.uploadDbToCloud(activity, id, object : UploadDbWorker.Listener {
                    override fun onSuccess() {
                        Assert.assertTrue(true)
                    }
                    override fun onFailure(error: String) {
                        Assert.assertTrue(error, false)
                    }
                    override fun onComplete() {
                        isRunning = false
                    }
                })
            }, onUnavailable = {

                val message = "*********** AdvertisingID is unavailable **************"
                println(message)
                isRunning = false
                Assert.assertTrue(message, false)
            }, onFailure = { error ->
                println("*********** $error **************")
                Assert.assertTrue(error, false)
                isRunning = false
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }

    }
}