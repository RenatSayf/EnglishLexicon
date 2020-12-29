package com.myapp.lexicon.wordstests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.helpers.RandomNumberGenerator
import java.util.*
import kotlin.collections.ArrayList


class OneOfFiveViewModel : ViewModel()
{
    init
    {
        println("********************** init() **************************")
    }

    private var _wordsList = MutableLiveData<ArrayList<Word>>()
    var wordsList: LiveData<ArrayList<Word>> = _wordsList
    fun initTest(list: ArrayList<Word>)
    {
        if (_wordsList.value.isNullOrEmpty())
        {
            _adapterList.value = list.take(ROWS) as ArrayList<Word>
            val randomIndex = RandomNumberGenerator(ROWS, (Date().time.toInt())).generate()
            _mysteryWord.value = _adapterList.value!![randomIndex].translate
            list.removeAll(_adapterList.value as ArrayList)
            _wordsList.value = list
        }
    }

    fun takeNextWord() : Word?
    {
        return _wordsList.value?.removeAt(0)
    }

    fun removeWord(word: Word) : Boolean
    {
        return _wordsList.value?.remove(word) ?: false
    }

    private var _mysteryWord = MutableLiveData<String>()
    var mysteryWord: LiveData<String> = _mysteryWord
    fun setMysteryWord(text: String)
    {
        _mysteryWord.value = text
    }

    private var _adapterList = MutableLiveData<ArrayList<Word>>()
    val adapterList: LiveData<ArrayList<Word>> = _adapterList

    private var _progress = MutableLiveData<Int>().apply {
        value = 0
    }
    var progress: LiveData<Int> = _progress
    fun setProgress(progress: Int)
    {
        _progress.value = progress
    }

    override fun onCleared()
    {
        super.onCleared()
        println("********************** onCleared() **************************")
    }

}