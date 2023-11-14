@file:Suppress("UNNECESSARY_SAFE_CALL")

package com.myapp.lexicon.main

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.database.models.Counters
import com.myapp.lexicon.helpers.checkSorting
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveWordToPref
import com.myapp.lexicon.settings.testIntervalFromPref
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
        private set

    var orderPlay: Int = 0
        private set

    val wordsInterval: Int
        get() {
            return app.testIntervalFromPref
        }

    init {
        initPlayList()
    }

    private var _wordsList = MutableLiveData<List<Word>>()
    @JvmField
    var wordsList: LiveData<List<Word>> = _wordsList

    fun initPlayList()
    {
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    setNewPlayList(word, 0)
                }
            },
            onSuccess = { word ->
                restorePlayList(word)
            },
            onFailure = { exception ->
                exception.throwIfDebug()
            }
        )
    }

    fun setNewPlayList(word: Word, order: Int)
    {
        viewModelScope.launch {
            val wordList = repository.getPlayListByDictNameAsync(word.dictName, order).await()
            orderPlay = wordList.checkSorting()
            displayedWordIndex = 0
            _wordsList?.value = wordList
        }
    }

    fun restorePlayList(word: Word) {
        viewModelScope.launch {
            val playList = repository.getPlayListAsync().await()
            displayedWordIndex = playList.indexOfFirst { it._id == word._id }
            val wordList = playList.map { it.toWord() }
            orderPlay = wordList.checkSorting()
            _wordsList?.value = wordList
        }
    }

    fun updatePlayList() {
        app.getWordFromPref(
            onSuccess = { word ->
                viewModelScope.launch {
                    val dicts = repository.getDictNameFromPlayListAsync().await()
                    val dictName = dicts.firstOrNull()
                    if (!dictName.isNullOrEmpty()) {
                        val playList = repository.getPlayListByDictNameAsync(dictName, orderPlay).await()
                        val foundIndex = playList.indexOfFirst { it._id == word._id }

                        if (foundIndex < 0) {
                            while (displayedWordIndex > 0) {
                                displayedWordIndex--
                                try {
                                    val previousWord = playList[displayedWordIndex]
                                    app.saveWordToPref(previousWord).also {
                                        displayedWordIndex = playList.indexOf(previousWord)
                                    }
                                    break
                                } catch (e: IndexOutOfBoundsException) {
                                    displayedWordIndex = 0
                                    val startWord = playList[0]
                                    app.saveWordToPref(startWord)
                                    break
                                }
                            }
                        }
                        _wordsList?.value = playList
                    }
                    else {
                        app.saveWordToPref(null)
                        initPlayList()
                    }
                }
            },
            onFailure = { exception ->
                exception.throwIfDebug()
            }
        )
    }

    fun wordListSize(): Int = _wordsList.value?.size ?: 0

    fun deleteDicts(
        dictList: List<String>,
        onSuccess: (Int) -> Unit,
        onNotFound: (String) -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentDicts = repository.getDictNameFromPlayListAsync().await()
                val currentDict = currentDicts.firstOrNull()
                val quantity = repository.deleteEntriesByDictNameAsync(dictList).await()
                if (quantity > 0) {
                    if (dictList.contains(currentDict)) {
                        app.saveWordToPref(null)
                        initPlayList()
                    }
                    onSuccess.invoke(quantity)
                }
                else {
                    onNotFound.invoke("Not found")
                }
            }
            catch (e: Exception) {
                onComplete.invoke(e)
            }
            finally {
                onComplete.invoke(null)
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

    private var _counters = MutableLiveData<Result<Counters>>()
    val counters: LiveData<Result<Counters>> = _counters

    fun getCountersById(id: Int) {
        viewModelScope.launch {
            try {
                val maps = repository.getCountersByIdAsync(id).await()
                val counters = maps.first()
                _counters.value = Result.success(counters)
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                _counters.value = Result.failure(e)
            }
        }
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