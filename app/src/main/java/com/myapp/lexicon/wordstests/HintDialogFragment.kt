package com.myapp.lexicon.wordstests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.models.Word
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HintDialogFragment : DialogFragment()
{
    companion object
    {
        val TAG = "${this::class.java.canonicalName}.TAG"
        private lateinit var targetWord: Word
        private lateinit var randomList: MutableList<Word>
        private var onItemSelected: (String) -> Unit = {}

        fun newInstance(
            targetWord: Word,
            randomList: MutableList<Word>,
            onItemSelected: (String) -> Unit
        ) : HintDialogFragment
        {
            this.targetWord = targetWord
            this.randomList = randomList
            this.onItemSelected = onItemSelected
            return HintDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.hint_dialog_fragment, container, false)
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        randomList.apply {
            add(targetWord)
            shuffle()
        }
        var ruArray: Array<String> = emptyArray()
        randomList.forEachIndexed { _, word ->
            ruArray += word.translate
        }

        val distinctArray = ruArray.distinct()

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(targetWord.english)
            setSingleChoiceItems(distinctArray.toTypedArray(), -1, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    onItemSelected.invoke(distinctArray[p1])
                    dismiss()
                }

            })
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white)
        }
    }

}