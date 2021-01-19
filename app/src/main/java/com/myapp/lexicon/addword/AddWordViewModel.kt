package com.myapp.lexicon.addword

import android.content.Context
import android.database.Cursor
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.database.DatabaseHelper
import androidx.lifecycle.LiveData
import io.reactivex.disposables.Disposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableEmitter
import com.myapp.lexicon.helpers.StringOperations
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.R
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.DataBaseQueries
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.*

class AddWordViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
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
                .subscribeOn(Schedulers.computation())
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