package com.myapp.lexicon.main

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.getOrderPlay
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveOrderPlay
import com.myapp.lexicon.settings.saveWordToPref
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataRepositoryImpl,
    private val app: Application
) : AndroidViewModel(app)
{
    private val composite = CompositeDisposable()

    var displayedWordIndex: Int = 0

    val displayedWord: Word?
        get() {
            return if (!_wordsList.value.isNullOrEmpty()) {
                _wordsList.value!![displayedWordIndex]
            }
            else null
        }

    init {
        refreshWordsList()
    }

    private var _wordsList = MutableLiveData<List<Word>>()
    @JvmField
    var wordsList: LiveData<List<Word>> = _wordsList

    fun refreshWordsList()
    {
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    setWordsList(word)
                }
            },
            onSuccess = { word ->
                setWordsList(word)
            },
            onFailure = {}
        )
    }

    fun setWordsList(word: Word, repeat: Int = 1)
    {
        composite.add(repository.getEntriesFromDbByDictName(word.dictName, 1, repeat, Int.MAX_VALUE)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val index = list.indexOfFirst {
                        it._id == word._id
                    }
                    displayedWordIndex = index
                    app.getOrderPlay(
                        onCycle = {
                            _wordsList.value = list
                        },
                        onRandom = {
                            _wordsList.value = list.shuffled()
                        }
                    )
                }, { t ->
                    t.message
                    t.printStackTrace()
                    _wordsList.value = listOf()
                }))
    }

    fun List<Word>.isSortedById(
        onASC: () -> Unit = {},
        onDESC: () -> Unit = {},
        onNotSorted: () -> Unit = {}
    ) {
        var res = this.zipWithNext { a, b ->
            a._id > b._id
        }.all { it }
        if (res) {
            onASC.invoke()
            return
        }

        res = this.zipWithNext { a, b ->
            a._id < b._id
        }.all { it }
        if (res) {
            onDESC.invoke()
            return
        }
        onNotSorted.invoke()
    }
    fun wordListSize(): Int = _wordsList.value?.size ?: 0

    fun shuffleWordsList()
    {
        val shuffledList = _wordsList.value?.toMutableList()?.shuffled()?: listOf()
        _wordsList.value = shuffledList
        app.saveOrderPlay(1)
        return
    }
    fun sortWordsList()
    {
        val sortedList = _wordsList.value?.toMutableList()?.sortedBy { word: Word -> word._id }?: listOf()
        _wordsList.value = sortedList
        app.saveOrderPlay(0)
        return
    }

    fun deleteDicts(
        dictList: List<String>,
        onSuccess: (Int) -> Unit,
        onNotFound: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val quantity = repository.deleteEntriesByDictNameAsync(dictList).await()
                if (quantity > 0) {
                    onSuccess.invoke(quantity)
                }
                else {
                    onNotFound.invoke("Not found")
                }
            } catch (e: Exception) {
                onFailure.invoke(e)
            }
        }
    }

    private var _dictionaryList = MutableLiveData<MutableList<String>>().apply {

        getDictList(onSuccess = { list ->
            value = list.toMutableList()
        }, onFailure = { t ->
            t.printStackTrace()
        })
    }
    val dictionaryList: LiveData<MutableList<String>> = _dictionaryList

    fun getDictList(onSuccess: (List<String>) -> Unit, onFailure: (Throwable) -> Unit) {

        composite.add(
            repository.getDictListFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    onSuccess.invoke(list)
                }, { t ->
                    onFailure.invoke(t)
                })
        )
    }

    fun setDictList(list: MutableList<String>)
    {
        _dictionaryList.value = list
    }

    fun saveCurrentWordToPref(word: Word)
    {
        app.saveWordToPref(word)
    }

    fun goForward(words: List<Word>)
    {
        repository.goForward(words)
    }

    private var _wordCounters = MutableLiveData<List<Int>>().apply {

        if (!_wordsList.value.isNullOrEmpty()) {
            composite.add(
                repository.getCountersFromDb(_wordsList.value!![0].dictName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ list ->
                        this.value = list
                    }, { t ->
                        t.printStackTrace()
                    })
            )
        }
    }
    var wordCounters: LiveData<List<Int>> = _wordCounters

    fun getCountersById(dictName: String, id: Int)
    {
        composite.add(
            repository.getCountersFromDb(dictName, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                     _wordCounters.value = list
                }, { t ->
                    t.printStackTrace()
                })
        )
    }

    private var _randomWord = MutableLiveData<Word>()

    fun getRandomWord(word: Word) : LiveData<Word>
    {
        viewModelScope.launch(Dispatchers.IO) {
            val dictName = word.dictName
            val id = word._id
            val randomWord = repository.getRandomEntriesFromDB(dictName, id).await()
            _randomWord.postValue(randomWord)
        }
        return _randomWord
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

    private var _isEndWordList = MutableLiveData(false)
    @JvmField
    val isEndWordList: LiveData<Boolean> = _isEndWordList
    fun wordsIsEnded(isEnd:Boolean)
    {
        _isEndWordList.value = isEnd
    }

    override fun onCleared()
    {
        composite.dispose()
        composite.clear()
        super.onCleared()
    }
}