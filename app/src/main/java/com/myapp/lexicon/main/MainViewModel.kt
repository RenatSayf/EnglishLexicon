package com.myapp.lexicon.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    private var _currentDict = MutableLiveData<String>()

    private var _wordsList = MutableLiveData<MutableList<Word>>()
    @JvmField
    var wordsList: MutableLiveData<MutableList<Word>> = _wordsList
    fun setWordsList(dictName: String, repeat: Int = 1)
    {
        composite.add(repository.getEntriesFromDbByDictName(dictName, 1, repeat, Int.MAX_VALUE)
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

    fun deleteDict(dictName: String) : Single<Boolean>
    {
        return repository.dropTableFromDb(dictName)
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
    val dictionaryList: LiveData<MutableList<String>> = _dictionaryList

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
        return@apply
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

    fun goForward(words: List<Word>)
    {
        repository.goForward(words)
    }

    private var _wordCounters = MutableLiveData<List<Int>>().apply {
        _currentWord.value?.let {
            repository.getCountersFromDb(it.dictName)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ list ->
                        this.value = list
                    }, { t ->
                        t.printStackTrace()
                    })
        }
    }
    var wordCounters: LiveData<List<Int>> = _wordCounters

    private var _randomWord = MutableLiveData<Word>().apply {
        val dictName = _currentWord.value?.dictName ?: "default"
        val id = _currentWord.value?._id ?: 1
        composite.add(repository.getRandomEntriesFromDB(dictName, id)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    value = it
                }, { t ->
                    t.printStackTrace()
                }))
    }

    fun getRandomWord(word: Word) : LiveData<Word>
    {
        return _randomWord.apply {
            val dictName = word.dictName
            val id = word._id
            composite.add(repository.getRandomEntriesFromDB(dictName, id)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        value = it
                    }, { t ->
                        t.printStackTrace()
                    }))
        }
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

    private var _intermediateIndex = MutableLiveData<Int>().apply {
        value = -1
    }
    fun setIntermediateIndex(index: Int)
    {
        _intermediateIndex.value = index
    }
    var intermediateIndex: LiveData<Int> = _intermediateIndex

    private var _countRepeat = MutableLiveData(0)
    var countRepeat: LiveData<Int> = _countRepeat
    fun setCountRepeat(repeat: Int, minId: Int, maxId: Int)
    {
        composite.add(repository.updateCountRepeat(repeat, minId, maxId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id ->
                    _countRepeat.value = id
                }, { t ->
                    t.printStackTrace()
                }))
    }


    init
    {
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