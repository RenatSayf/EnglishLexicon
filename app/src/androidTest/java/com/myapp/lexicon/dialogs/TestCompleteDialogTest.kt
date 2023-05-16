package com.myapp.lexicon.dialogs

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import com.myapp.lexicon.R
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class TestCompleteDialogTest {

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
    fun onCreateDialog_Success_result() {

        var isRunning = true

        scenario.onActivity { activity ->

            ConfirmDialog.newInstance(onLaunch = { dialog, binding ->
                with(binding) {
                    ivIcon.visibility = View.INVISIBLE
                    tvEmoji.text = activity.getString(R.string.slightly_smiling_face)
                    tvEmoji2.apply {
                        text = activity.getString(R.string.thumbs_up)
                        visibility = View.VISIBLE
                    }
                    tvMessage.text = activity.getString(R.string.text_test_passed)
                    btnCancel.visibility = View.INVISIBLE
                    btnOk.text = activity.getString(R.string.text_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        isRunning = false
                    }
                }
            }).show(activity.supportFragmentManager, ConfirmDialog.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun onCreateDialog_Error_result() {

        var isRunning = true

        scenario.onActivity { activity ->

            ConfirmDialog.newInstance(onLaunch = { dialog, binding ->
                with(binding) {
                    ivIcon.visibility = View.INVISIBLE
                    tvEmoji.text = activity.getString(R.string.confused_face)
                    tvEmoji2.apply {
                        text = activity.getString(R.string.thumbs_up)
                        visibility = View.GONE
                    }
                    tvMessage.text = activity.getString(R.string.text_test_not_passed)
                    btnCancel.visibility = View.INVISIBLE
                    btnOk.text = activity.getString(R.string.text_repeat)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        isRunning = false
                    }
                }
            }).show(activity.supportFragmentManager, ConfirmDialog.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}