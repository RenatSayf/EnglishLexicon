package com.myapp.lexicon.cloudstorage

import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream


const val TEST_ADS_ID = "Test-55555-44444-33333-22222-11111"
const val TEST_DB_NAME = "test_data_base.db"

@RunWith(AndroidJUnit4ClassRunner::class)
class UploadDbWorkerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity { activity ->
            try {
                activity.databaseList().first {
                    it == TEST_DB_NAME
                }
            } catch (e: NoSuchElementException) {
                val inputStream = activity.assets.open("databases/$TEST_DB_NAME")
                val bytes = inputStream.readBytes()
                val dbPath = activity.databaseList().first {
                    it == "lexicon_DB.db"
                }
                val dbFolder = activity.getDatabasePath(dbPath).parent
                val file = File(dbFolder, TEST_DB_NAME)
                if (!file.exists()) {
                    file.createNewFile()
                    FileOutputStream(file).apply {
                        this.write(bytes)
                        this.close()
                    }
                }
            }
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
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }

    }
}