package com.myapp.lexicon.billing

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.models.PurchaseToken
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class BillingViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var billingVM: BillingViewModel

    @Before
    fun setUp() {
        scenario = rule.scenario
        billingVM = BillingViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun checkNoAdsProduct() {

        var isRunning = true

        scenario.onActivity { activity ->

            billingVM.noAdsProduct.observe(activity) { result ->
                result.onSuccess {
                    val productId = it.productId
                    Assert.assertEquals("no_ads", productId)
                    isRunning = false
                }
                result.onFailure {
                    it.printStackTrace()
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun checkCloudStorageProduct() {
        var isRunning = true

        scenario.onActivity { activity ->

            billingVM.cloudStorageProduct.observe(activity) { result ->
                result.onSuccess { details ->
                    val productId = details.productId
                    Assert.assertEquals("cloud_storage", productId)
                    isRunning = false
                }
                result.onFailure {
                    it.printStackTrace()
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun purchaseCloudStorage() {
        var isRunning = true

        scenario.onActivity { activity ->

            billingVM.cloudStorageProduct.observe(activity) { result ->
                result.onSuccess {
                    billingVM.purchaseProduct(activity, it)
                }
                result.onFailure {
                    println(it.message)
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }

            billingVM.cloudStorageToken.observe(activity) { result ->
                result.onSuccess {
                    isRunning = when(it) {
                        PurchaseToken.YES -> {
                            Assert.assertTrue(it.name, true)
                            false
                        }
                        PurchaseToken.NO -> {
                            Assert.assertTrue(it.name, false)
                            false
                        }
                    }
                }
                result.onFailure {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }
        }
        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun purchaseNoAds() {

        var isRunning = true

        scenario.onActivity { activity ->

            billingVM.noAdsProduct.observe(activity) { result ->
                result.onSuccess {
                    billingVM.noAdsProduct.value?.let {
                        it.onSuccess { details ->
                            billingVM.purchaseProduct(activity, details)
                        }
                    }
                }
                result.onFailure {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }

            billingVM.noAdsToken.observe(activity) { token ->
                when(token) {
                    PurchaseToken.YES -> {
                        Assert.assertTrue(token.name, true)
                        isRunning = false
                    }
                    PurchaseToken.NO -> {
                        Assert.assertTrue(token.name, false)
                        isRunning = false
                    }
                    else -> {}
                }
            }
        }
        while (isRunning) {
            Thread.sleep(100)
        }
    }
}