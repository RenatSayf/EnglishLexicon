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

    private var _deletedId = MutableLiveData<Int>(0)
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
    var dictsToMove: LiveData<MutableList<String>> = _dictsToMove
    fun setDictsToMove(list: MutableList<String>)
    {
        _dictsToMove.value = list
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