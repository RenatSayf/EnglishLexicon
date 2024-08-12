package com.myapp.lexicon.ads

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.interfaces.IAdDataListener
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class BannersFragmentTest {

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

        scenario.onActivity { act ->

            val bannersFragment = BannersFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(
                        BannersFragment.ARG_ID_LIST,
                        arrayListOf(
                            //BannerAdIds.BANNER_3.id,
                            BannerAdIds.BANNER_4.id,
                            BannerAdIds.BANNER_5.id,
                            //BannerAdIds.BANNER_2.id
                        )
                    )
                }
            }

            bannersFragment.setAdDataListener(object : IAdDataListener {
                override fun onDismissed(data: AdData?) {
                    if (data != null) {
                        val actualRevenue = data.revenue
                        println("************ actualRevenue = $actualRevenue *****************")
                        Assert.assertTrue(true)
                    }
                    else {
                        Assert.assertTrue(false)
                    }
                    Thread.sleep(5000)
                    isRunning = false
                }
            })

            act.supportFragmentManager.beginTransaction()
                .add(R.id.frameLayout, bannersFragment)
                .commit()
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}