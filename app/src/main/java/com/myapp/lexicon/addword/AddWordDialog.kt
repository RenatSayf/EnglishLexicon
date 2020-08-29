package com.myapp.lexicon.addword

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.database.LexiconDataBase
import com.myapp.lexicon.dialogs.NewDictDialog
import com.myapp.lexicon.settings.AppData
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.add_word_dialog.*

class AddWordDialog : DialogFragment(), NewDictDialog.INewDictDialogResult
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
    private lateinit var viewModel: LexiconDataBase
    private var subscriber: Disposable? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        activity?.let { a ->

            viewModel = ViewModelProvider(this)[LexiconDataBase::class.java]

            dialogView = a.layoutInflater.inflate(R.layout.add_word_dialog, LinearLayout(a), false)

            val builder = AlertDialog.Builder(a).setView(dialogView)
            return builder.create().apply {
                window?.setBackgroundDrawableResource(R.drawable.add_word_background)
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

            if (viewModel.dictionaries.value.isNullOrEmpty())
            {
                subscriber = viewModel.setDictList(a)
            }

            viewModel.dictionaries.observe(viewLifecycleOwner, Observer { list ->
                if (!list.isNullOrEmpty())
                {
                    val ndict = AppData.getInstance().ndict
                    val adapter = ArrayAdapter(a, R.layout.app_spinner_item, list)
                    dictListSpinner.adapter = adapter
                    if (ndict > -1)
                    {
                        dictListSpinner.setSelection(ndict)
                    }
                }
            })

            dictListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, p3: Long)
                {
                    view?.let {
                        viewModel.setSelected(index)
                        val text = (view as TextView).text
                        if (text == getString(R.string.text_new_dict))
                        {
                            NewDictDialog.newInstance().apply {
                                setNewDictDialogListener(this@AddWordDialog)
                            }.run {
                                show(a.supportFragmentManager, NewDictDialog.TAG)
                            }
                        }
                    }
                    return
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {

                }
            }

            btnOk.setOnClickListener {
                val text = (dictListSpinner.selectedView as TextView).text
                if (text == getString(R.string.text_new_dict))
                {
                    NewDictDialog.newInstance().apply {
                        setNewDictDialogListener(this@AddWordDialog)
                    }.run {
                        show(a.supportFragmentManager, NewDictDialog.TAG)
                    }
                }
                else
                {

                }
            }
            btnCancel.setOnClickListener {

            }
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

        viewModel.spinnerSelectedIndex().observe(viewLifecycleOwner, Observer {
            dictListSpinner.setSelection(it)
        })
    }

    override fun newDictDialogResult(res: Boolean, dictName: String?)
    {
        if (res) viewModel.setDictList(activity)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        subscriber?.dispose()
    }


}