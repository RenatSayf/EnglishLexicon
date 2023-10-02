package com.myapp.lexicon.service

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ServiceActivityTest {

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
    fun onCreate() {
        var isRunning = true

        scenario.onActivity { act ->

            val words = listOf(
                Word(1, "Test", "english", "английский", 1)
            )
            val json = words.toString()
            val intent = Intent(act, ServiceActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(ServiceActivity.ARG_JSON, json)
            }
            act.startActivity(intent)

            act.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Assert.assertTrue(true)
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}