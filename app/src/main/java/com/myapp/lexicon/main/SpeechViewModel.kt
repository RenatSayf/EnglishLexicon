package com.myapp.lexicon.main

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.settings.isEngSpeech
import com.myapp.lexicon.settings.isRuSpeech
import java.util.*


class SpeechViewModel(
    private val app: Application
) : AndroidViewModel(app),
    Speaker.Listener
{
    class Factory(private val app: Application): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == SpeechViewModel::class.java)
            return SpeechViewModel(app) as T
        }
    }

    private var _enCheckboxEnable = MutableLiveData(true)
    var enCheckboxEnable: LiveData<Boolean> = _enCheckboxEnable

    private var _ruCheckboxEnable = MutableLiveData(true)
    var ruCheckboxEnable: LiveData<Boolean> = _ruCheckboxEnable

    private var speaker: Speaker = Speaker(app, this)

    override fun onSuccessInit() {}

    override fun onSpeechStart(id: String)
    {
        _speechStartId.postValue(id)
    }

    override fun onSpeechDone(id: String)
    {
        _speechDoneId.postValue(id)
    }

    override fun onSpeechError(id: String)
    {
        _speechErrorId.postValue(id)
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onSpeechInitNotSuccess(status: Int)
    {
        if (status < 0)
        {
            _enCheckboxEnable?.value = false
            _ruCheckboxEnable?.value = false
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onEngLangNotSupported(status: Int)
    {
        app.isEngSpeech = false
        _enCheckboxEnable?.value = false
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onRusLangNotSupported(status: Int)
    {
        app.isRuSpeech = false
        _ruCheckboxEnable?.value = false
    }

    private var _speechStartId = MutableLiveData<String>().apply {
        value = ""
    }
    var speechStartId: LiveData<String> = _speechStartId

    private var _speechDoneId = MutableLiveData<String>().apply {
        value = ""
    }
    var speechDoneId: LiveData<String> = _speechDoneId

    private var _speechErrorId = MutableLiveData<String>().apply {
        value = ""
    }
    var speechErrorId: LiveData<String> = _speechErrorId

    private var _speechError = MutableLiveData(Int.MIN_VALUE)
    var speechError: LiveData<Int> = _speechError

    fun doSpeech(text: String, locale: Locale)
    {
        speaker.doSpeech(text, locale)
    }

    fun stopSpeech() : Int
    {
        return speaker.stop()
    }

    private var _isEnSpeech = MutableLiveData(app.isEngSpeech)
    var isEnSpeech: LiveData<Boolean> = _isEnSpeech
    fun enableEnSpeech(flag: Boolean) {
        _isEnSpeech.value = flag
    }

    private var _isRuSpeech = MutableLiveData(app.isRuSpeech)
    var isRuSpeech: LiveData<Boolean> = _isRuSpeech
    fun enableRuSpeech(flag: Boolean) {
        _isRuSpeech.value = flag
    }

    private var _speechProgressVisibility = MutableLiveData(View.INVISIBLE)
    var speechProgressVisibility: LiveData<Int> = _speechProgressVisibility
    fun setSpeechProgressVisibility(visibility: Int)
    {
        _speechProgressVisibility.value = visibility
    }

    override fun onCleared()
    {
        app.isEngSpeech = _isEnSpeech.value?: true
        app.isRuSpeech = _isRuSpeech.value?: false
        super.onCleared()
    }
}