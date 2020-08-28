package com.myapp.lexicon.addword

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.database.LexiconDataBase
import kotlinx.android.synthetic.main.add_word_dialog.*

class AddWordDialog : DialogFragment()
{
    companion object
    {
        const val TAG = "translator_dialog"
        private const val WORD_LIST_TAG = "en_word"
        private val instance : AddWordDialog? = null

        fun getInstance(list: ArrayList<String>) : AddWordDialog = instance ?: AddWordDialog().apply {
            arguments = Bundle().apply {
                putStringArrayList(WORD_LIST_TAG, list)
            }
        }
    }

    private var dialogView: View? = null
    private var inputList: ArrayList<String> = arrayListOf()
    private lateinit var db: LexiconDataBase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        activity?.let { a ->

            db = ViewModelProvider(this)[LexiconDataBase::class.java]

            dialogView = a.layoutInflater.inflate(R.layout.add_word_dialog, LinearLayout(a), false)

            val builder = AlertDialog.Builder(a).setView(dialogView)
            return builder.create().apply {
                window?.setBackgroundDrawableResource(R.color.colorPrimaryDarkTransparent)
            }
        } ?:
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return dialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        activity?.let { a ->
            db.setDictList(a).observe(viewLifecycleOwner, Observer { list ->
                if (!list.isNullOrEmpty())
                {
                    val adapter = ArrayAdapter(a, android.R.layout.simple_list_item_1, list)
                    dictListSpinner.adapter = adapter
                }
            })
        }

        arguments?.let{
            inputList = it.getStringArrayList(WORD_LIST_TAG) as ArrayList<String>
            inputList.size.let{s ->
                if (s > 1)
                {
                    inputWordTV.text = inputList[0]
                    translateTV.setText(inputList[1])
                }
            }
        }
    }


}