package com.myapp.lexicon.dialogs

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.R
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ConfirmDialogTest {

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
    fun onCreateDialog() {
        var isRunning = true

        scenario.onActivity { act ->
            ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
                with(binding) {
                    dialog.isCancelable = true
                    ivIcon.visibility = View.INVISIBLE
                    tvEmoji2.visibility = View.GONE
                    tvEmoji.apply {
                        visibility = View.VISIBLE
                        text = act.getString(R.string.coins_bag)
                    }
                    val message = "Some message for user Some message for user Some message for user Some message for user"
                    tvMessage.text = message
                    btnCancel.visibility = View.GONE
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        isRunning = false
                    }
                }
            }).show(act.supportFragmentManager, ConfirmDialog.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}