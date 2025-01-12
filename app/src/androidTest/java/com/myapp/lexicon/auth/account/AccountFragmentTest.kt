package com.myapp.lexicon.auth.account

import androidx.appcompat.widget.AppCompatEditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.MockAuthViewModel
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.main.viewmodels.MockUserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AccountFragmentTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    init {
        require(BuildConfig.ADS_SOURCE == AdsSource.LOCAL_HOST.name, lazyMessage = {
            "**************** BuildVariants LOCAL_HOST is required *********************"
        })
    }

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun onCreateView_Init() {

        scenario.onActivity { act ->

            val user = User("XxXxXx").apply {
                email = "test_email@emai.com"
                phone = "+79998887755"
                bankName = "Тест Банк"
                bankCard = "1111111"
                userReward = 10.0
            }
            MockUserViewModel.testData = user

            val accountFragment = AccountFragment.newInstance(
                authVMClass = MockAuthViewModel::class.java,
                accountVMClass = MockAccountViewModel::class.java,
                userVMClass = MockUserViewModel::class.java
            )
            act.supportFragmentManager
                .beginTransaction()
                .add(R.id.frameLayout, accountFragment, "XXX")
                .commitNow()

            val tvFirstName = act.findViewById<AppCompatEditText>(R.id.tvEmailValue)
            val actualBgState = tvFirstName.background.constantState
            val expectedBgState = act.resources.getDrawable(R.drawable.bg_horizontal_oval, null).constantState
            Assert.assertEquals(expectedBgState, actualBgState)
        }

        Thread.sleep(10000)
        Espresso.onView(ViewMatchers.withText(MockAccountViewModel.testMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        Thread.sleep(2000)
        Espresso.onView(ViewMatchers.withId(R.id.tvEmailValue))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }
}