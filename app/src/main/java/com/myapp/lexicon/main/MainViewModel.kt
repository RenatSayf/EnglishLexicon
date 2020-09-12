package com.myapp.lexicon.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private var subscribe: Disposable? = null

    private val _dictList : MutableLiveData<LinkedList<String>> by lazy {
        MutableLiveData<LinkedList<String>>().also {
            loadDictList()
        }
    }
    fun getDictList() : LiveData<LinkedList<String>>
    {
        return _dictList
    }

    private fun loadDictList()
    {
        subscribe = repository.getTableListFromDb()
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ list: LinkedList<String>? ->
                    _dictList.postValue(list)
                }, { t: Throwable? ->
                    _dictList.value = LinkedList()
                    t?.printStackTrace()
                })
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
        subscribe = repository.getAllFromTable(dictName)
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ list: LinkedList<DataBaseEntry>? ->

                    val list1 = list

                }, { t: Throwable? ->

                })
    }

    override fun onCleared()
    {
        subscribe?.dispose()
        super.onCleared()
    }
}