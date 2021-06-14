package com.myapp.lexicon.addword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.DataBaseQueries
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class AddWordViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    private val _spinnerSelectedIndex = MutableLiveData<Int>()
    fun spinnerSelectedIndex(): LiveData<Int>
    {
        return _spinnerSelectedIndex
    }

    fun setSelected(index: Int)
    {
        _spinnerSelectedIndex.value = index
    }

    fun insertInTableAsync(context: Context?, tableName: String?, entry: DataBaseEntry?): Observable<Long?>
    {
        return Observable.create { emitter: ObservableEmitter<Long?> ->
            try
            {
                val res = DataBaseQueries(context).insertWordInTableSync(tableName, entry)
                emitter.onNext(res)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
            finally
            {
                emitter.onComplete()
            }
        }
    }
    private var _insertedId = MutableLiveData<Long>().apply {
        value = 0
    }
    var insertedId: LiveData<Long> = _insertedId

    fun insertEntryAsync(word: Word)
    {
        composite.add(repository.insertEntry(word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id ->
                    _insertedId.value = id
                }, { t ->
                    t.printStackTrace()
                }))

    }

    override fun onCleared()
    {
        super.onCleared()
        composite.run {
            dispose()
            clear()
        }
    }
}