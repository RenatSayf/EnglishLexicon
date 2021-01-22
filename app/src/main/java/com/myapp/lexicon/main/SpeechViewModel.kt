package com.myapp.lexicon.main

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

@Suppress("ObjectLiteralToLambda")
class SpeechViewModel @ViewModelInject constructor(private val app: Application, private val repository: DataRepositoryImpl) : AndroidViewModel(app), Speaker.IOnSpeechListener
{
    private lateinit var speaker: Speaker
    private val composite = CompositeDisposable()

    init
    {
        speaker = Speaker(app, object : TextToSpeech.OnInitListener
        {
            override fun onInit(status: Int)
            {
                speaker.speechInit(status, app, speaker)
                speaker.setOnSpeechListener(this@SpeechViewModel)
            }
        })
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

    override fun onContinued(arg: String)
    {

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
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _speechError.postValue(it)
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

    override fun onCleared()
    {
        super.onCleared()
        composite.run {
            dispose()
            clear()
        }
    }
}