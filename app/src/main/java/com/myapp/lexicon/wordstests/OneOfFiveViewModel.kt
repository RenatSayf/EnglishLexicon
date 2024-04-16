package com.myapp.lexicon.wordstests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.models.Word


class OneOfFiveViewModel : ViewModel()
{

    private var _wordsList = MutableLiveData<ArrayList<Word>>()
    var wordsList: LiveData<ArrayList<Word>> = _wordsList
    fun initTest(list: List<Word>)
    {
        _progressMax.value = list.size
        if (_wordsList.value.isNullOrEmpty() && list.isNotEmpty())
        {
            _adapterList.value = list.take(ROWS) as MutableList<Word>
            _mysteryWord.value = _adapterList.value!!.random().translate
            _wordsList.value = list as ArrayList<Word>
            _wordsList.value?.removeAll((_adapterList.value as MutableList).toSet())
        }
    }

    fun takeNextWord() : Word?
    {
        return _wordsList.value?.removeAt(0)
    }

    private var _mysteryWord = MutableLiveData<String>()
    var mysteryWord: LiveData<String> = _mysteryWord
    fun setMysteryWord(text: String)
    {
        _mysteryWord.value = text
    }

    private var _adapterList = MutableLiveData<MutableList<Word>>()
    val adapterList: LiveData<MutableList<Word>> = _adapterList

    private var _progress = MutableLiveData<Int>().apply {
        value = 0
    }
    var progress: LiveData<Int> = _progress
    fun setProgress(progress: Int)
    {
        _progress.value = progress
    }

    private var _progressMax = MutableLiveData<Int>()
    var progressMax: LiveData<Int> = _progressMax

    private var _wrongAnswerCount = MutableLiveData<Int>().apply {
        value = 0
    }
    var wrongAnswerCount: LiveData<Int> = _wrongAnswerCount
    fun increaseWrongAnswerCount()
    {
        _wrongAnswerCount.value = _wrongAnswerCount.value?.plus(1)
        return
    }

}