package com.myapp.lexicon.wordeditor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class EditorViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    private var _wordsList = MutableLiveData(mutableListOf<Word>())
    @JvmField
    val wordsList: LiveData<MutableList<Word>> = _wordsList

    private var _deletedId = MutableLiveData(0)
    var deletedId: LiveData<Int> = _deletedId

    fun deleteWordFromDb(word: Word)
    {
        composite.add(repository.deleteEntry(word)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id ->
                    _deletedId.value = id
                }, { t ->
                    _deletedId.value = -1
                    t.printStackTrace()
                }))
    }

    private var _dictsToMove = MutableLiveData<MutableList<String>>()
    val dictsToMove: LiveData<MutableList<String>> = _dictsToMove
    fun setDictsToMove(list: MutableList<String>)
    {
        _dictsToMove.value = list
    }

    private var _wordIsStudied = MutableLiveData<Boolean?>(null)
    var wordIsStudied: LiveData<Boolean?> = _wordIsStudied
    fun disableWord(isDisable: Boolean)
    {
        _wordIsStudied.value = isDisable
    }

    private var _isMoveWord = MutableLiveData<Boolean?>(null).apply {
        value = false
    }
    var isMoveWord: LiveData<Boolean?> = _isMoveWord
    fun setMoveWord(isMove: Boolean)
    {
        _isMoveWord.value = isMove
    }

    private var _enWord = MutableLiveData("")
    val enWord: LiveData<String> = _enWord
    fun setEnWord(text: String)
    {
        _enWord.value = text
    }

    private var _ruWord = MutableLiveData("")
    val ruWord: LiveData<String> = _ruWord
    fun setRuWord(text: String)
    {
        _ruWord.value = text
    }

    private var _isWordUpdated = MutableLiveData<Boolean?>(null)
    @JvmField
    var isWordUpdated: LiveData<Boolean?> = _isWordUpdated

    fun updateWordInDb(words: List<Word>)
    {
        composite.add(
            repository.updateEntries(words)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id ->
                    _isWordUpdated.value = id > 0
                }, { t ->
                    t.printStackTrace()
                })
        )
    }

    init
    {
        repository.getWordFromPref().apply {
            composite.add(
                repository.getEntriesFromDbByDictName(this.dictName, 1, -1, Int.MAX_VALUE)
                    .subscribe({ list ->
                        _wordsList.value = list
                    }, { t ->
                        t.printStackTrace()
                    })
            )
        }
    }

    override fun onCleared()
    {
        super.onCleared()
        composite.apply {
            dispose()
            clear()
        }
    }
}