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
class UserPurchaseTest {

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
    fun fromJson() {
        var isRunning = true

        scenario.onActivity {
            val json =
                """{"orderId":"GPA.3332-8875-6276-26783","packageName":"com.myapp.lexicon","productId":"cloud_storage","purchaseTime":1690035819196,"purchaseState":0,"purchaseToken":"XXXXXX","quantity":1,"acknowledged":false}"""
            val userPurchase = UserPurchase.fromJson(json)
            Assert.assertEquals("GPA.3332-8875-6276-26783", userPurchase.orderId)

            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}