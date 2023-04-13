package com.myapp.lexicon.main

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@Suppress("ObjectLiteralToLambda")
@HiltViewModel
class SpeechViewModel @Inject constructor(app: Application, private val repository: DataRepositoryImpl) : AndroidViewModel(app),
    Speaker.Listener
{
    private var speaker: Speaker = Speaker(app, this)
    private val composite = CompositeDisposable()

    override fun onSuccessInit() {

    }

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

    override fun onSpeechInitNotSuccess(status: Int)
    {
        if (status < 0)
        {
            repository.enableSpeech(false)
            _enCheckboxEnable.value = false
            _ruCheckboxEnable.value = false
        }
    }

    override fun onEngLangNotSupported(status: Int)
    {
        setEnSpeech(false)
        _enCheckboxEnable.value = false
    }

    override fun onRusLangNotSupported(status: Int)
    {
        setRuSpeech(false)
        _ruCheckboxEnable.value = false
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
        composite.add(doSpeechAsync(text, locale)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _speechError.value = it
                }, { t ->
                    t.printStackTrace()
                }))
    }

    private fun doSpeechAsync(text: String, locale: Locale) : Single<Int>
    {
        return Single.create {
            try
            {
                it.onSuccess(speaker.doSpeech(text, locale))
            }
            catch (e: Exception)
            {
                it.onError(e)
            }
        }
    }

    fun stopSpeech() : Int
    {
        return speaker.stop()
    }

    private var _isEnSpeech = MutableLiveData(repository.isEngSpeech())
    var isEnSpeech: LiveData<Boolean> = _isEnSpeech
    fun setEnSpeech(isSpeech: Boolean)
    {
        _isEnSpeech.value = isSpeech
        repository.setEngSpeech(isSpeech)
    }

    private var _isRuSpeech = MutableLiveData(repository.isRusSpeech())
    var isRuSpeech: LiveData<Boolean> = _isRuSpeech
    fun setRuSpeech(isSpeech: Boolean)
    {
        _isRuSpeech.value = isSpeech
        repository.setRusSpeech(isSpeech)
    }

    private var _speechProgressVisibility = MutableLiveData(View.INVISIBLE)
    var speechProgressVisibility: LiveData<Int> = _speechProgressVisibility
    fun setSpeechProgressVisibility(visibility: Int)
    {
        _speechProgressVisibility.value = visibility
    }

    private var _enCheckboxEnable = MutableLiveData(true)
    var enCheckboxEnable: LiveData<Boolean> = _enCheckboxEnable

    private var _ruCheckboxEnable = MutableLiveData(true)
    var ruCheckboxEnable: LiveData<Boolean> = _ruCheckboxEnable

    override fun onCleared()
    {
        super.onCleared()
        composite.run {
            dispose()
            clear()
        }
    }
}