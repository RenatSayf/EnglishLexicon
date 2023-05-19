package com.myapp.lexicon.schedule

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AlarmSchedulerTest {

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
    fun scheduleOne() {

        var isRunning = true
        var scheduler: AlarmScheduler? = null

        scenario.onActivity { activity ->

            scheduler = AlarmScheduler(activity)
            scheduler!!.scheduleOne(5000)

            Thread.sleep(10000)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
        scheduler?.cancel(AlarmScheduler.ONE_SHOOT_ACTION)
    }
}