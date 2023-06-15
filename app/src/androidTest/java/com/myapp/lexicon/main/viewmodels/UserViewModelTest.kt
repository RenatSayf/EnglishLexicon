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

        val localUser = User("123456789").apply {
            reward = 0.005
            currency = "USD"
        }

        val remoteUserData = mapOf(
            User.KEY_REWARD to "0.002",
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]
            val actualReward = userViewModel.calculateReward(localUser, remoteUserData)
            Assert.assertEquals(0.005, actualReward, 0.0)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateReward_incorrect_remote_data() {
        var isRunning = true

        val localUser = User("123456789").apply {
            reward = 0.002
            currency = "USD"
        }

        val remoteUserData = mapOf(
            User.KEY_REWARD to "0.0XX",
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]
            val actualReward = userViewModel.calculateReward(localUser, remoteUserData)
            Assert.assertEquals(0.001, actualReward, 0.0)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateReward_empty_remote_data() {
        var isRunning = true

        val localUser = User("123456789").apply {
            reward = 0.002
            currency = "USD"
        }

        val remoteUserData = mapOf(
            User.KEY_REWARD to null,
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]
            val actualReward = userViewModel.calculateReward(localUser, remoteUserData)
            Assert.assertEquals(0.001, actualReward, 0.0)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun calculateTotalRevenue_min_value() {

        var isRunning = true

        val localUser = User("123456789").apply {
            totalRevenue = 0.002
            reward = 0.002
            currency = "USD"
        }

        val remoteUserData = mapOf(
            User.KEY_TOTAL_REVENUE to "0.004",
            User.KEY_REWARD to "0.002",
            User.KEY_CURRENCY to "USD"
        )

        scenario.onActivity { activity ->

            val userViewModel = ViewModelProvider(activity)[UserViewModel::class.java]
            val actualRevenue = userViewModel.calculateTotalRevenue(localUser, remoteUserData)
            Assert.assertEquals(0.006, actualRevenue, 0.0)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}