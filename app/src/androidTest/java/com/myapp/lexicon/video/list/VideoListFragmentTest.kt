package com.myapp.lexicon.video.list

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.repository.network.MockNetRepository
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import com.myapp.lexicon.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


@RunWith(AndroidJUnit4ClassRunner::class)
class VideoListFragmentTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var repository: MockNetRepository

    @Before
    fun setUp() {
        scenario = rule.scenario
        repository = MockNetRepository()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun onCreateView() {
        var isRunning = true

        scenario.onActivity { act ->
            runBlocking {
                val fragment = VideoListFragment.newInstance()
                act.supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment, "XXX").commitNow()

                delay(40000)
                isRunning = false
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}