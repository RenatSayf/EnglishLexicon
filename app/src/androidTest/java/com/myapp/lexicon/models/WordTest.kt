package com.myapp.lexicon.models

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


@RunWith(AndroidJUnit4ClassRunner::class)
class WordTest {

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
    fun toWord() {
        var isRunning = true

        scenario.onActivity { act ->
            val string =
                """{"_id":887,"countRepeat":1,"dictName":"My dict","english":"continue","translate":"продолжить"}"""
            val word = string.toWord()
            Assert.assertEquals(887, word._id)
            Assert.assertEquals(1, word.countRepeat)
            Assert.assertEquals("My dict", word.dictName)
            Assert.assertEquals("continue", word.english)
            Assert.assertEquals("продолжить", word.translate)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}