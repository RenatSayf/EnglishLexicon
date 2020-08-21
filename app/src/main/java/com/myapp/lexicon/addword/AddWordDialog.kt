package com.myapp.lexicon.addword

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.lexicon.R
import kotlinx.android.synthetic.main.add_word_dialog.*
import kotlinx.android.synthetic.main.add_word_dialog.view.*

class AddWordDialog : DialogFragment()
{
    companion object
    {
        val TAG = "translator_dialog"
        private val EN_WORD_TAG = "en_word"
        private val instace : AddWordDialog? = null

        fun getInstance() : AddWordDialog = instace ?: AddWordDialog()
    }

    private var dialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        activity?.let { a ->

            dialogView = a.layoutInflater.inflate(R.layout.add_word_dialog, LinearLayout(a), false)


            val builder = AlertDialog.Builder(a).setView(dialogView)
            return builder.create()
        } ?:
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        TranslateFragment.translateEvent.observe(viewLifecycleOwner, Observer {
            val translateListAdapter = it.getContent()?.let { content -> TranslateListAdapter(content) }
            val linearLayoutManager = LinearLayoutManager(context)
            val defaultItemAnimator = DefaultItemAnimator()

            translateRV.apply {
                adapter = translateListAdapter
                layoutManager = linearLayoutManager
                itemAnimator = defaultItemAnimator
            }
        })
        return dialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

    }


}