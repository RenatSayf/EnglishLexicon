package com.myapp.lexicon.addword

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.AddWordDialogBinding
import com.myapp.lexicon.dialogs.NewDictDialog
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.hideKeyboard
import com.myapp.lexicon.helpers.showKeyboard
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.Speaker
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.getWordFromPref
import java.util.Locale


class AddWordDialog : DialogFragment(),
    NewDictDialog.Listener,
    Speaker.Listener
{
    companion object
    {
        val TAG = "${this::class.java.simpleName}.TAG"
        private const val WORD_LIST_TAG = "en_word"
        private var listener: Listener? = null

        fun newInstance(
            list: List<String>,
            listener: Listener? = null
        ) : AddWordDialog = AddWordDialog().apply {
            this@Companion.listener = listener
            arguments = Bundle().apply {
                putStringArrayList(WORD_LIST_TAG, (list as java.util.ArrayList<String>))
            }
        }
    }

    interface Listener {
        fun onCancelClick()
        fun onDismiss()
    }

    private lateinit var binding: AddWordDialogBinding

    private var inputList: ArrayList<String> = arrayListOf()
    private val addWordVM: AddWordViewModel by viewModels()
    private val mainVM: MainViewModel by viewModels()
    private val speaker: Speaker by lazy {
        Speaker(requireContext(), this)
    }
    private var newDictName: String? = null
    private val locker by lazy {
        LockOrientation(requireActivity())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        locker.lock()
        setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog)
        isCancelable = false
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = AddWordDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            mainVM.dictionaryList.observe(viewLifecycleOwner) { list ->
                if (!list.isNullOrEmpty()) {

                    val adapter = ArrayAdapter(requireContext(), R.layout.app_spinner_item, list.distinct())
                    dictListSpinner.adapter = adapter

                    requireContext().getWordFromPref(
                        onInit = {},
                        onSuccess = { word, _ ->

                            val index = list.indexOf(word.dictName)
                            if (index >= 0) {
                                when (newDictName) {
                                    null -> {
                                        dictListSpinner.setSelection(index)
                                    }
                                    else -> {
                                        val i = list.indexOf(newDictName)
                                        dictListSpinner.setSelection(i)
                                    }
                                }
                            }
                        },
                        onFailure = {}
                    )
                }
            }

            btnNewDict.setOnClickListener {
                NewDictDialog.newInstance(this@AddWordDialog).show(parentFragmentManager, NewDictDialog.TAG)
            }

            editBtnView.setOnClickListener {
                translateTV.apply {
                    if (!this.isFocused)
                    {
                        isEnabled = true
                        requestFocus()
                        this.setSelection(this.text.length)
                        this.showKeyboard()
                    }
                    else
                    {
                        clearFocus()
                        isEnabled = false
                        this.hideKeyboard()
                    }
                }
            }


            btnAdd.setOnClickListener {
                val dictName = (dictListSpinner.selectedView as TextView).text.toString()
                if (!translateTV.text.isNullOrEmpty()) {
                    val enWord = inputWordTV.text.toString()
                    val ruWord = translateTV.text.toString()
                    val word = Word(0, dictName, enWord, ruWord, 1)
                    setFragmentResult(TranslateFragment.KEY_ADD_WORD, Bundle().apply {
                        putString(TranslateFragment.KEY_NEW_WORD, word.toString())
                    })
                }
                dismiss()
            }

            btnCancel.setOnClickListener {
                dismiss()
                listener?.onCancelClick()
            }

            enSpeechBtn.setOnClickListener {
                inputWordTV.text?.let {
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

            ruSpeechBtn.setOnClickListener {
                translateTV.text?.let {
                    speaker.doSpeech(it.toString(), Locale.getDefault())
                }
            }
        }

        arguments?.let{
            inputList = it.getStringArrayList(WORD_LIST_TAG) as ArrayList<String>
            inputList.size.let{ s ->
                if (s > 1)
                {
                    binding.inputWordTV.text = inputList[0]
                    binding.translateTV.setText(inputList[1])
                }
            }
        }

        addWordVM.spinnerSelectedIndex().observe(viewLifecycleOwner) {
            binding.dictListSpinner.setSelection(it)
        }

    }

    override fun onPositiveClick(dictName: String)
    {
        val oldList = mainVM.dictionaryList.value
        oldList?.let {
            it.add(0, dictName)
            mainVM.setDictList(it)
            addWordVM.setSelected(0)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {

        listener?.onDismiss()
        locker.unLock()
        super.onDismiss(dialog)
    }

    override fun onDestroy()
    {
        speaker.shutdown()
        listener = null
        super.onDestroy()
    }

    override fun onSuccessInit() {

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

    override fun onSpeechInitNotSuccess(status: Int)
    {

    }

    override fun onEngLangNotSupported(status: Int)
    {

    }

    override fun onRusLangNotSupported(status: Int)
    {

    }

    override fun onNegativeClick() {

    }


}