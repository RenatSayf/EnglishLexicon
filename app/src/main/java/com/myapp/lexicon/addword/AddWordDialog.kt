package com.myapp.lexicon.addword

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.dialogs.NewDictDialog
import com.myapp.lexicon.helpers.Keyboard
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.Speaker
import com.myapp.lexicon.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.add_word_dialog.*
import java.util.*


@AndroidEntryPoint
class AddWordDialog : DialogFragment(), NewDictDialog.INewDictDialogResult, Speaker.IOnSpeechListener
{
    companion object
    {
        val TAG = "${this::class.java.canonicalName}.TAG"
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
    private lateinit var adwvm: AddWordViewModel
    private lateinit var vm: MainViewModel
    private var subscriber: Disposable? = null
    private lateinit var speaker: Speaker
    private var newDictName: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        requireActivity().let { a ->
            adwvm = ViewModelProvider(this)[AddWordViewModel::class.java]
            vm = ViewModelProvider(this)[MainViewModel::class.java]
            dialogView = a.layoutInflater.inflate(R.layout.add_word_dialog, LinearLayout(a), false)

            val builder = AlertDialog.Builder(a).setView(dialogView)
            return builder.create().apply {
                window?.setBackgroundDrawableResource(R.drawable.add_word_background)
            }
        }
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        speaker = Speaker(activity, object : TextToSpeech.OnInitListener
        {
            override fun onInit(status: Int)
            {
                speaker.speechInit(status, activity, speaker)
                speaker.setOnSpeechListener(this@AddWordDialog)
            }
        })
        return dialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        val dictListSpinner = dialogView?.findViewById<Spinner>(R.id.dictListSpinner)
        val inputWordTV = dialogView?.findViewById<TextView>(R.id.inputWordTV)
        val btnOk = dialogView?.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogView?.findViewById<Button>(R.id.btnCancel)
        val editBtnView = dialogView?.findViewById<ImageButton>(R.id.editBtnView)
        val enSpeechBtn = dialogView?.findViewById<ImageButton>(R.id.enSpeechBtn)
        val ruSpeechBtn = dialogView?.findViewById<ImageButton>(R.id.ruSpeechBtn)
        val translateTV = dialogView?.findViewById<EditText>(R.id.translateTV)

        requireActivity().let { a ->

            vm.dictionaryList.observe(viewLifecycleOwner, { list ->
                list.add(getString(R.string.text_new_dict))
                if (!list.isNullOrEmpty())
                {
                    val dictName = vm.currentWord.value?.dictName
                    dictName?.let {
                        val index = list.indexOf(it)
                        if (index > 0)
                        {
                            val adapter = ArrayAdapter(a, R.layout.app_spinner_item, list.distinct())
                            dictListSpinner?.adapter = adapter
                            when
                            {
                                newDictName == null && index > -1 ->
                                {
                                    dictListSpinner?.setSelection(index)
                                }
                                newDictName != null ->
                                {
                                    val i = list.indexOf(newDictName)
                                    dictListSpinner?.setSelection(i)
                                }
                            }
                        }
                    }

                }
            })

            dictListSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, p3: Long)
                {
                    view?.let {
                        adwvm.setSelected(index)
                        when ((view as TextView).text)
                        {
                            getString(R.string.text_new_dict) ->
                            {
                                NewDictDialog.newInstance().apply {
                                    setNewDictDialogListener(this@AddWordDialog)
                                }.run {
                                    show(a.supportFragmentManager, NewDictDialog.TAG)
                                }
                            }
                        }
                    }
                    return
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {

                }
            }

            editBtnView?.setOnClickListener {
                translateTV?.apply {
                    if (!this.isFocused)
                    {
                        isEnabled = true
                        requestFocus()
                    }
                    else
                    {
                        clearFocus()
                        isEnabled = false
                        Keyboard.getInstance().forceHide(a, this)
                    }
                }
            }


            btnOk?.setOnClickListener {
                when (val dictName = (dictListSpinner?.selectedView as TextView).text.toString())
                {
                    getString(R.string.text_new_dict) ->
                    {
                        NewDictDialog.newInstance().apply {
                            setNewDictDialogListener(this@AddWordDialog)
                        }.run {
                            show(a.supportFragmentManager, NewDictDialog.TAG)
                        }
                    }
                    else ->
                    {
                        (!translateTV?.text.isNullOrEmpty()).run {
                            if (this)
                            {

                                if (inputWordTV != null && translateTV != null)
                                {
                                    val enWord = inputWordTV.text.toString()
                                    val ruWord = translateTV.text.toString()
                                    val word = Word(0, dictName, enWord, ruWord, 1)
                                    adwvm.insertedId.observe(viewLifecycleOwner, {
                                        if (it > 0)
                                        {
                                            val toast = Toast.makeText(a, getString(R.string.in_dictionary) + dictName + getString(R.string.new_word_is_added), Toast.LENGTH_SHORT)
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                        }
                                    })
                                    adwvm.insertEntryAsync(word)
                                    dismiss()
                                }
//                                val entry = DataBaseEntry(inputWordTV?.text.toString(), translateTV?.text.toString())
//                                subscriber = adwvm.insertInTableAsync(a, dictName.toString(), entry)
//                                        .subscribeOn(Schedulers.newThread())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe({ long: Long? ->
//                                            long?.let{
//                                                if (long > -1)
//                                                {
//                                                    val toast = Toast.makeText(a, getString(R.string.in_dictionary) + dictName + getString(R.string.new_word_is_added), Toast.LENGTH_SHORT)
//                                                    toast.setGravity(Gravity.CENTER, 0, 0)
//                                                    toast.show()
//                                                }
//                                            }
//                                            dismiss()
//                                        }, { e: Throwable? ->
//                                            e?.printStackTrace()
//                                            dismiss()
//                                        })
                            }
                        }
                    }
                }
            }

            btnCancel?.setOnClickListener {
                dismiss()
            }

            enSpeechBtn?.setOnClickListener {
                inputWordTV?.text?.let {
                    try
                    {
                        speaker.doSpeech(it.toString(), Locale.US)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }

            ruSpeechBtn?.setOnClickListener {
                translateTV?.text?.let {
                    speaker.doSpeech(it.toString(), Locale.getDefault())
                }
            }
        }

        arguments?.let{
            inputList = it.getStringArrayList(WORD_LIST_TAG) as ArrayList<String>
            inputList.size.let{ s ->
                if (s > 1)
                {
                    inputWordTV?.text = inputList[0]
                    translateTV?.setText(inputList[1])
                }
            }
        }

        adwvm.spinnerSelectedIndex().observe(viewLifecycleOwner, {
            dictListSpinner?.setSelection(it)
        })

//        adwvm.insertedId.observe(viewLifecycleOwner, {
//
//        })
    }

    override fun newDictDialogResult(dictName: String)
    {
        val oldList = vm.dictionaryList.value
        oldList?.let {
            it.add(0, dictName)
            vm.setDictList(it)
            adwvm.setSelected(0)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        subscriber?.dispose()
        speaker.shutdown()
    }

    override fun onSpeechStart(id: String?)
    {

    }

    override fun onSpeechDone(id: String?)
    {

    }

    override fun onSpeechError(id: String?)
    {

    }

    override fun onContinued(arg: String?)
    {
        AppSettings(requireContext()).isEngSpeech = false
    }


}