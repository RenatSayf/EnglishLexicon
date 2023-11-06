package com.myapp.lexicon.main

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.getWordFromPref
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

    private var _currentDict = MutableLiveData<String>()

    @JvmField
    var currentDict: LiveData<String> = _currentDict

    fun setCurrentDict(dictName: String)
    {
        _currentDict.value = dictName
    }

    private var _wordsList = MutableLiveData<MutableList<Word>>()
    @JvmField
    var wordsList: MutableLiveData<MutableList<Word>> = _wordsList

    fun refreshWordsList()
    {
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    setWordsList(word.dictName)
                }
            },
            onSuccess = { word ->
                setWordsList(word.dictName)
            },
            onFailure = {}
        )
    }

    fun resetWordsList()
    {
        composite.add(
            repository.getDictListFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    if (list.isNotEmpty())
                    {
                        setWordsList(list.first())
                        saveCurrentWordToPref(Word(1, list.first(), "", "", 1))
                        _currentDict.value = list.first()
                    }
                    else
                    {
                        setWordsList("XXXXXXXXXXXX")
                        _currentDict.value = ""
                    }
                }, { e ->
                    e.printStackTrace()
                })
        )
    }

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

    private var _currentWord = MutableLiveData<Word>().apply {

        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    postValue(word)
                }
            },
            onSuccess = { word ->
                postValue(word)
            },
            onFailure = {

            }
        )
    }
    var currentWord: MutableLiveData<Word> = _currentWord
    fun setCurrentWord(word: Word)
    {
        _currentWord.value = word
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
        _currentWord.value?.let {
            composite.add(
                repository.getCountersFromDb(it.dictName)
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

    init
    {
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    _currentDict.value = word.dictName
                    setWordsList(word.dictName)
                }
            },
            onSuccess = { word ->
                _currentDict.value = word.dictName
                setWordsList(word.dictName)
            },
            onFailure = {}
        )
    }

    override fun onCleared()
    {
        composite.dispose()
        composite.clear()
        super.onCleared()
    }
}