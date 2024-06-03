package com.myapp.lexicon.bgwork

import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.helpers.toStringTime
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.util.Locale


@RunWith(AndroidJUnit4ClassRunner::class)
class ResetDailyRewardWorkTest {

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
    fun calculateLaunchTime() {
        var isTestComplete = false

        scenario.onActivity { act ->

            val inputTimeStr = "23:59 +0300"
            val localeRu = Locale("Ru", "ru")
            val launchTime: Long? = ResetDailyRewardWork.calculateLaunchTime(inputTimeStr)
            val actualTimeStr = launchTime?.toStringTime()

            val currentTimeInMillis = Calendar.getInstance(localeRu).timeInMillis
            val duration = Duration.ofMillis(launchTime?.minus(currentTimeInMillis) ?: -1)
            val hours = duration.toHours()

            val expectedTimeStr = "${currentTimeInMillis.toStringDate(localeRu)} 23:59:00"
            Assert.assertEquals(expectedTimeStr, actualTimeStr)

            isTestComplete = true
        }

        while (!isTestComplete) {
            Thread.sleep(100)
        }
    }
}