package com.myapp.lexicon.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    fun getDictList() : Observable<LinkedList<String>>
    {
        return repository.getTableListFromDb()
    }

    private var _playList = MutableLiveData<LinkedList<String>>().apply {
        val list = repository.getTableListFromSettings()
        postValue(list)
    }

    var playList : LiveData<LinkedList<String>> = _playList

    fun setPlayList()
    {
        _playList.value = repository.getTableListFromSettings()
    }

    fun getAllWordsFromDict(dictName: String)
    {
         val subscribe = repository.getAllFromTable(dictName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ list: LinkedList<DataBaseEntry>? ->

                    val list1 = list

                }, { e: Throwable? ->
                    e?.printStackTrace()
                })
        composite.add(subscribe)
    }

    fun deleteDict(dictName: String) : Single<Boolean>
    {
        return repository.dropTableFromDb(dictName)
    }

    override fun onCleared()
    {
        composite.dispose()
        super.onCleared()
    }
}