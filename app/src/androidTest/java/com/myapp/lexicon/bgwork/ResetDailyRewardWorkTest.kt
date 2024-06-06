package com.myapp.lexicon.bgwork

import android.icu.util.Calendar
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
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

        val localeRu = Locale("Ru", "ru")
        val calendar = Calendar.getInstance(localeRu).apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val duration: Duration? = ResetDailyRewardWork.calculateInitialDelay(calendar.timeInMillis)

        val actualHours = duration?.toHours()

        Assert.assertEquals(20, actualHours)
    }
}