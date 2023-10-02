package com.myapp.lexicon.wordstests

import androidx.activity.OnBackPressedCallback
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
import java.util.Locale


@RunWith(AndroidJUnit4ClassRunner::class)
class TestFragmentTest {

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
    fun speechRecognize() {
        var isRunning = true

        scenario.onActivity {
            val localeUS = Locale.US
            val usTag = localeUS.toLanguageTag()
            val localeRU = Locale("ru", "RU")
            val ruTag = localeRU.toLanguageTag()

            Assert.assertEquals("en-US", usTag)
            Assert.assertEquals("ru-RU", ruTag)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun onCreateView() {
        var isRunning = true

        scenario.onActivity { act ->

            act.supportFragmentManager.beginTransaction().add(
                act.binding.frameLayout.id,
                TestFragment.newInstance(null)
            ).commit()

            act.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isRunning = false
                }
            })
        }

        while (isRunning) {
            Thread.sleep(100)
        }

    }
}