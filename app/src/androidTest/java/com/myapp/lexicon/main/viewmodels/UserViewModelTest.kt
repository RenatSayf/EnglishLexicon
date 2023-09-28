package com.myapp.lexicon.main.viewmodels

import androidx.lifecycle.ViewModelProvider
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
class UserViewModelTest {

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
    fun calculateReward_correct_remote_data() {
        var isRunning = true

        val revenuePerAd = 10.0
        val startUserReward = 7

        val remoteUserData = mapOf(
            User.KEY_USER_REWARD to startUserReward,
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]

            val actualReward = userViewModel.calcUserReward(revenuePerAd, 0.7, remoteUserData)
            val expectedReward = startUserReward + (revenuePerAd) * UserViewModel.USER_PERCENTAGE

            Assert.assertEquals(expectedReward, actualReward, 0.001)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateReward_incorrect_remote_data() {
        var isRunning = true

        val revenuePerAd = 10.0

        val remoteUserData = mapOf(
            User.KEY_USER_REWARD to "7.0XX",
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]

            val actualReward = userViewModel.calcUserReward(revenuePerAd, 0.7, remoteUserData)
            Assert.assertEquals(revenuePerAd, actualReward, 0.001)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateReward_empty_remote_data() {
        var isRunning = true

        val revenuePerAd = 10.0

        val remoteUserData = mapOf(
            User.KEY_USER_REWARD to null,
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]
            val actualReward = userViewModel.calcUserReward(revenuePerAd, 0.7, remoteUserData)
            Assert.assertEquals(revenuePerAd, actualReward, 0.001)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateReallyRevenue_and_UserReward() {

        var isRunning = true

        val remoteUserData = mapOf(
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]

            val revenuePerAd = 10.0
            val expectedPercentage = 0.7

            val actualReward = userViewModel.calcUserReward(revenuePerAd, expectedPercentage, remoteUserData)

            val actualPercentage = actualReward / revenuePerAd
            Assert.assertEquals(expectedPercentage, actualPercentage, 0.05)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}