package com.myapp.lexicon.billing

import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.testing.TestActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                    val expectedId = activity.getString(R.string.id_no_ads)
                    Assert.assertEquals(expectedId, productId)
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
                    val expectedId = activity.getString(R.string.id_cloud_storage)
                    Assert.assertEquals(expectedId, productId)
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

            activity.lifecycleScope.launch {
                delay(120000)
                Assert.assertTrue("********** Test timeout ***********", false)
                isRunning = false
            }

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
                    isRunning = when {
                        it.isNotEmpty() -> {
                            Assert.assertTrue(true)
                            false
                        }
                        else -> {
                            Assert.assertTrue(false)
                            false
                        }
                    }
                }
                result.onFailure {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }

            billingVM.wasCancelled.observe(activity) { result ->
                result.onSuccess { details ->
                    val productId = details.productId
                    val expectedId = activity.getString(R.string.id_cloud_storage)
                    Assert.assertEquals(expectedId, productId)
                    isRunning = false
                }
                result.onFailure {
                    Assert.assertTrue(false)
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

            activity.lifecycleScope.launch {
                delay(120000)
                Assert.assertTrue("********** Test timeout ***********", false)
                isRunning = false
            }

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

            billingVM.noAdsToken.observe(activity) { result ->

                result.onSuccess { token ->
                    isRunning = when {
                        token.isNotEmpty() -> {
                            Assert.assertTrue(true)
                            false
                        }
                        else -> {
                            Assert.assertTrue(false)
                            false
                        }
                    }
                }
                result.onFailure {
                    Assert.assertTrue(it.message, false)
                    isRunning = false
                }
            }

            billingVM.wasCancelled.observe(activity) { result ->
                result.onSuccess { details ->
                    val productId = details.productId
                    val expectedId = activity.getString(R.string.id_no_ads)
                    Assert.assertEquals(expectedId, productId)
                    isRunning = false
                }
                result.onFailure {
                    Assert.assertTrue(false)
                    isRunning = false
                }
            }
        }
        while (isRunning) {
            Thread.sleep(100)
        }
    }
}