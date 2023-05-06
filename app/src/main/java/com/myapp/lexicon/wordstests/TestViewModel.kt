package com.myapp.lexicon.wordstests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.helpers.UiState
import com.myapp.lexicon.models.TestState
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.getWordFromPref
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TestViewModel @Inject constructor(
    app: Application,
    private val repository: DataRepositoryImpl
) : AndroidViewModel(app)
{
    private val composite = CompositeDisposable()

    private var _liveState = MutableLiveData<UiState>(UiState.NotActive(0))
    var liveState: LiveData<UiState> = _liveState
    fun setLiveState(state: UiState)
    {
        _liveState.value = state
    }

    var testState = TestState()

    private var _currentWord = MutableLiveData<Word>().apply {
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    value = repository.getFirstEntryAsync().await()
                }
            },
            onSuccess = { word ->
                value = word
            },
            onFailure = {}
        )
    }
    var currentWord: LiveData<Word> = _currentWord

    private var _dictList = MutableLiveData<List<String>>(listOf()).apply {
        composite.add(
            repository.getDictListFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ dicts ->
                    value = dicts
                },{ t ->
                    t.printStackTrace()
                })
        )
    }
    var dictList: LiveData<List<String>> = _dictList

    private var _wordsList = MutableLiveData<MutableList<Word>>(arrayListOf(Word(-1, "", "", "", 0)))
    var wordsList: LiveData<MutableList<Word>> = _wordsList

    fun getWordsByDictName(dict: String): LiveData<Result<List<Word>>>
    {
        val result = MutableLiveData<Result<List<Word>>>(Result.failure(Throwable()))
        composite.add(
            repository.getEntriesFromDbByDictName(dict, 1, 1, Int.MAX_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val filteredList = list.filter {
                        !testState.studiedWordIds.contains(it._id)
                    }.toMutableList()
                    filteredList.shuffle()
                    _wordsList.value = filteredList
                    result.value = Result.success(filteredList)

                    if (filteredList.isNotEmpty()) {
                        if (filteredList.first().dictName == testState.dict) {
                            testState.dict = filteredList.first().dictName
                            testState.progressMax = list.size
                            _wordsCount.value = list.size
                            _wordIndex.value = testState.progress
                        }
                        else {
                            testState.reset()
                            _wordsCount.value = list.size
                            _wordIndex.value = 0
                        }
                    }


                    rightAnswerCounter = 0
                }, { t ->
                    t.printStackTrace()
                    result.value = Result.failure(t)
                })
        )
        return result
    }

    fun getWordsByIds(ids: List<Int>)
    {
        composite.add(
            repository.getEntriesByIds(ids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    _wordsList.value = list
                    _wordsCount.value = list.size
                    _wordIndex.value = 0
                    rightAnswerCounter = 0
                }, { t ->
                    t.printStackTrace()
                })
        )
    }

    private var _similarWords = MutableLiveData<MutableList<Word>>(arrayListOf())
    var similarWords: LiveData<MutableList<Word>> = _similarWords

    fun getAllSimilarWords(dict: String, like: String)
    {
        composite.add(
            repository.getAllSimilarEntriesFromDB(dict, like)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ words ->
                   _similarWords.value = words
                }, {
                    it.printStackTrace()
                })
        )
    }

    private var _isRight = MutableLiveData<Boolean?>(null)
    var isRight: LiveData<Boolean?> = _isRight
    fun resetRight()
    {
        _isRight.value = null
    }

    fun searchWord(word: Word)
    {
        val filteredWords = _wordsList.value!!.filter {
            it.english == word.english && it.translate == word.translate
        }
        _isRight.value = _wordsList.value!!.removeAll(filteredWords)
        _isRight.value?.let {
            if (it && _wordsList.value!!.isNotEmpty())
            {
                _wordIndex.value = _wordsCount.value!! - _wordsList.value!!.size
            }
            else if (it && _wordsList.value!!.isEmpty())
            {
                _wordIndex.value = _wordsCount.value
            }
        }
    }

    fun getNextWords() : Word?
    {
        return if (_wordsList.value!!.isNotEmpty())
        {
            _wordsList.value!![0]
        }
        else
        {
            null
        }
    }

    private var _wordsCount = MutableLiveData<Int>().apply {
        value = _wordsList.value?.size?: 0
    }
    var wordsCount: LiveData<Int> = _wordsCount

    private var _wordIndex = MutableLiveData(0)
    var wordIndex: LiveData<Int> = _wordIndex

    fun saveWordIdsToPref(words: MutableList<Word>)
    {
        val idList = arrayListOf<Int>()
        words.forEach {
            idList.add(it._id)
        }
        //println("************************ ${idList.joinToString()} ******************************")
        repository.saveWordsIdStringToPref(idList.joinToString())
    }

    fun getWordIdsFromPref() : List<Int>
    {
        val wordsIdString = repository.getWordsIdStringFromPref()
        val intList = arrayListOf<Int>()
        if (wordsIdString.isNotEmpty())
        {
            val strList = wordsIdString.split(",")

            strList.forEach {
                intList.add(it.trim().toInt())
            }
        }
        return intList
    }

    var rightAnswerCounter = 0

    override fun onCleared()
    {
        composite.run {
            dispose()
            clear()
        }
        _liveState.value = UiState.NotActive()
        super.onCleared()
    }

    init
    {
        _liveState.value = UiState.Initial()
    }
}