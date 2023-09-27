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
        val startUserReward = 7.0

        val remoteUserData = mapOf(
            User.KEY_USER_REWARD to startUserReward.toString(),
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]

            val actualReward = userViewModel.calcUserReward(revenuePerAd, remoteUserData)
            val expectedReward = startUserReward + (revenuePerAd) * UserViewModel.USER_PERCENTAGE

            Assert.assertEquals(expectedReward, actualReward, 0.0)

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

            val actualReward = userViewModel.calcUserReward(revenuePerAd, remoteUserData)
            Assert.assertTrue(actualReward < 0)

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
            val actualReward = userViewModel.calcUserReward(revenuePerAd, remoteUserData)
            Assert.assertTrue(actualReward < 0)

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
            User.KEY_USER_REWARD to "7.0",
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]

            val revenuePerAd = 10.0

            val actualReward = userViewModel.calcUserReward(revenuePerAd, remoteUserData)

            val actualPercentage = actualReward / revenuePerAd
            Assert.assertEquals(UserViewModel.USER_PERCENTAGE, actualPercentage, 0.05)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}