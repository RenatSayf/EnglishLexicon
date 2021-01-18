package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R

class TestCompleteDialog : DialogFragment()
{

    companion object
    {
        val TAG = "${this::class.java.canonicalName}.TAG"
        private var instance: TestCompleteDialog? = null
        private var errorCount: Int = 0
        lateinit var listener: ITestCompleteDialogListener
        @JvmStatic
        fun getInstance(errorCount: Int, listener: ITestCompleteDialogListener): TestCompleteDialog
        {
            this.errorCount = errorCount
            this.listener = listener
            return if (instance == null)
            {
                TestCompleteDialog()
            }
            else instance as TestCompleteDialog
        }
    }

    interface ITestCompleteDialogListener
    {
        fun onTestPassed()
        fun onTestFailed(errors: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setCancelable(false)
            when (errorCount)
            {
                0 ->
                {
                    val smilingIcon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.icon_smiling_face, null)
                    setIcon(smilingIcon)
                    setTitle(getString(R.string.text_test_passed))
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        listener.onTestPassed()
                        dismiss()
                    }
                }
                else ->
                {
                    val sadIcon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.icon_sad_face, null)
                    setIcon(sadIcon)
                    setTitle(getString(R.string.text_test_not_passed))
                    setPositiveButton(getString(R.string.text_repeat)) { _, _ ->
                        listener.onTestFailed(errorCount)
                        dismiss()
                    }
                }
            }
        }
        return builder.create()
    }
}