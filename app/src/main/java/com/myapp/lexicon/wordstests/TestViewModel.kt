package com.myapp.lexicon.wordstests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject


@HiltViewModel
class TestViewModel @Inject constructor(app: Application, private val repository: DataRepositoryImpl) : AndroidViewModel(app)
{
    private val composite = CompositeDisposable()

    private var _currentWord = MutableLiveData<Word>().apply {
        value = repository.getWordFromPref()
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

    private var _wordsList = MutableLiveData<MutableList<Word>>(arrayListOf())
    var wordsList: LiveData<MutableList<Word>> = _wordsList
    fun getWordsByDictName(dict: String)
    {
        composite.add(
            repository.getEntriesFromDbByDictName(dict, 1, Int.MAX_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                   _wordsList.value = list
                }, { t ->
                    t.printStackTrace()
                })
        )
    }

    override fun onCleared()
    {
        composite.run {
            dispose()
            clear()
        }
        super.onCleared()
    }

    init
    {
        _currentWord.value?.let { getWordsByDictName(it.dictName) }
    }
}