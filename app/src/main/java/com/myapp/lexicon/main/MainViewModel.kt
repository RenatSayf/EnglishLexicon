package com.myapp.lexicon.main

import android.graphics.drawable.Drawable
import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    private var _currentDict = MutableLiveData<String>()
    var currentDict : LiveData<String> = _currentDict

    private var _playList = MutableLiveData<MutableList<String>>()
    var playList : LiveData<MutableList<String>> = _playList

    private fun setPlayList()
    {
        _playList.value = repository.getTableListFromSettings()
    }

    private var _wordsList = MutableLiveData<MutableList<Word>>()
    var wordsList: MutableLiveData<MutableList<Word>> = _wordsList
    fun setWordsList(dictName: String)
    {
        composite.add(getWordsFromDict(dictName, 1, Int.MAX_VALUE)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    _wordsList.value = list
                }, { t ->
                    t.message
                    t.printStackTrace()
                    _wordsList.value = mutableListOf()
                }))
    }
    fun wordListSize(): Int = _wordsList.value?.size ?: 0

    fun shuffleWordsList()
    {
        _wordsList.value?.shuffle()
        _wordsList.value = _wordsList.value
        return
    }
    fun sortWordsList()
    {
        _wordsList.value?.sortBy { word: Word -> word._id  }
        _wordsList.value = _wordsList.value

        return
    }

    fun getWordsFromDict(dictName: String, id: Int, limit: Int): Single<MutableList<Word>>
    {
         return repository.getEntriesFromDbByDictName(dictName, id, limit)
    }

    fun deleteDict(dictName: String) : Single<Boolean>
    {
        return repository.dropTableFromDb(dictName)
    }

    fun getEntriesAndCounters(dictName: String, rowId: Int, order: String, limit: Int = 2): Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        return repository.getEntriesAndCountersFromDb(dictName, rowId, order, limit)
    }

    fun getRandomEntries(dictName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>
    {
        return repository.getRandomEntriesFromDb(dictName, rowId)
    }

    private var _dictionaryList = MutableLiveData<MutableList<String>>().apply {
        getDictList().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    value = list
                }, { t ->
                    t.printStackTrace()
                })
    }
    var dictionaryList: LiveData<MutableList<String>> = _dictionaryList

    fun getDictList() : Single<MutableList<String>>
    {
        return repository.getDictListFromDb()
    }

    fun setDictList(list: MutableList<String>)
    {
        _dictionaryList.value = list
    }

    private var _currentWord = MutableLiveData<Word>().apply {
        //repository.saveWordThePref(Word(1, "Наречия", "", "", 1))
        value = repository.getWordFromPref()
    }
    var currentWord: MutableLiveData<Word> = _currentWord
    fun setCurrentWord(word: Word)
    {
        _currentWord.value = word
    }
    fun saveCurrentWordToPref(word: Word)
    {
        repository.saveWordThePref(word)
    }

    var testInterval: LiveData<Int> = MutableLiveData(repository.getTestIntervalFromPref())

    private var _orderPlay = MutableLiveData<Int>().apply {
        value = repository.getOrderPlay()
    }
    val orderPlay: LiveData<Int> = _orderPlay
    fun setOrderPlay(order: Int)
    {
        _orderPlay.value = order
        repository.saveOrderPlay(order)
    }

    private var _mainControlVisibility = MutableLiveData<Int>().apply {
        value = View.VISIBLE
    }
    var mainControlVisibility: LiveData<Int> = _mainControlVisibility
    fun setMainControlVisibility(viability: Int)
    {
        _mainControlVisibility.value = viability
    }


    init
    {
        _playList.value = repository.getTableListFromSettings() as ArrayList<String>
        _currentDict.value = repository.getWordFromPref().dictName
        val dictName = _currentDict.value
        if (!dictName.isNullOrEmpty())
        {
            setWordsList(dictName)
        }
    }

    override fun onCleared()
    {

        composite.dispose()
        composite.clear()
        super.onCleared()
    }
}