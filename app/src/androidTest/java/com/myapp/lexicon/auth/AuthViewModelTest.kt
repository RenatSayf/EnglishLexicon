package com.myapp.lexicon.auth

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AuthViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var authVM: AuthViewModel

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity {
            authVM = ViewModelProvider(it)[AuthViewModel::class.java]
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun registerWithEmailAndPassword() {

        var isRunning = true

        scenario.onActivity { activity ->

            authVM.registerWithEmailAndPassword("renatsayf@gmail.com", "123456")

            authVM.state.observe(activity) { state ->
                state.onSignUp {
                    Assert.assertTrue(true)
                    isRunning = false
                }
                state.onExists {
                    println("************ EMAIL_ALREADY_IN_USE ***************************")
                    Assert.assertTrue(true)
                    isRunning = false
                }
                state.onFailure { ex ->
                    Assert.assertTrue(ex.message, false)
                    isRunning = false
                }
                state.onEmailValid {
                    Assert.assertTrue(false)
                    isRunning = false
                }
                state.onPasswordValid {
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
    fun signInWithEmailAndPassword() {
        var isRunning = true

        scenario.onActivity { activity ->
            authVM.signInWithEmailAndPassword("renatsayf@gmail.com", "123456")

            authVM.state.observe(activity) { state ->
                state.onSignIn {
                    Assert.assertTrue(true)
                    isRunning = false
                }
                state.onFailure {
                    Assert.assertTrue(it.message?: "******** Unknown error **********", false)
                    isRunning = false
                }
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun resetPassword() {
        var isRunning = true

        scenario.onActivity { activity ->

            authVM.resetPassword("renatsayf@gmail.com")
            authVM.state.observe(activity) { state ->
                isRunning = when(state) {
                    is UserState.Failure -> {
                        Assert.assertTrue(state.error.message, false)
                        false
                    }
                    UserState.PasswordReset -> {
                        Assert.assertTrue(true)
                        false
                    }
                    UserState.NotRegistered -> {
                        true
                    }
                    else -> {
                        Assert.assertTrue(false)
                        false
                    }
                }
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }


}