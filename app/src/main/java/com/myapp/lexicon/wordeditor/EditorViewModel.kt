package com.myapp.lexicon.wordeditor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers



class EditorViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
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

    override fun onCleared()
    {
        super.onCleared()
        composite.apply {
            dispose()
            clear()
        }
    }
}