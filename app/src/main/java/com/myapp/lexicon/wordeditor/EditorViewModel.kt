package com.myapp.lexicon.wordeditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.common.OrderBy
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.settings.getWordFromPref
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch


class EditorViewModel constructor(
    private val repository: DataRepositoryImpl,
    app: Application
) : AndroidViewModel(app)
{
    class Factory(private val app: Application): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == EditorViewModel::class.java)
            return EditorViewModel(
                repository = DataRepositoryImpl(AppDataBase.getDbInstance(app.applicationContext).appDao()),
                app
            ) as T
        }
    }

    private val composite = CompositeDisposable()

    private var _wordsList = MutableLiveData(mutableListOf<Word>())
    @JvmField
    val wordsList: LiveData<MutableList<Word>> = _wordsList

    var switcherChildIndex = 0

    fun getAllWordsByDictName(dict: String)
    {
        composite.add(
            repository.getEntriesFromDbByDictName(dictName = dict, id = 1, repeat = -1, orderBy = OrderBy.ASC, limit = Int.MAX_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    _wordsList.value = list
                }, { t ->
                    t.printStackTrace()
                })
        )
    }

    private var _deletedId = MutableLiveData(0)
    var deletedId: LiveData<Int> = _deletedId

    fun deleteWordFromDb(word: Word)
    {
        composite.add(repository.deleteEntry(word)
                .subscribeOn(Schedulers.io())
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
    @JvmField
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

    private var _isWordUpdated = MutableLiveData<Boolean?>(null).apply {
        value = false
    }
    @JvmField
    var isWordUpdated: LiveData<Boolean?> = _isWordUpdated

    @JvmField
    var selectedWord: Word? = null

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
        app.getWordFromPref(
            onInit = {
                viewModelScope.launch {
                    val word = repository.getFirstEntryAsync().await()
                    if (word != null) {
                        getAllWordsByDictName(word.dictName)
                    }
                    else {
                        getAllWordsByDictName("Наречия")
                    }
                }
            },
            onSuccess = { word, _ ->
                getAllWordsByDictName(word.dictName)
            },
            onFailure = {}
        )
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