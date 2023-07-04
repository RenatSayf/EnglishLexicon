package com.myapp.lexicon.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.models.User
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AuthFragmentTest {

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
    fun onViewCreated() {
        var isRunning = true

        scenario.onActivity { activity ->

            val fragment = AuthFragment.newInstance(object : AuthFragment.AuthListener {
                override fun refreshAuthState(user: User) {}
            })
            activity.supportFragmentManager.beginTransaction().add(
                fragment,
                AuthFragment.TAG
            ).commit()

            Thread.sleep(1000)

            isRunning = if (fragment.isVisible) {
                Assert.assertTrue(true)
                false
            } else {
                Assert.assertTrue(false)
                false
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}