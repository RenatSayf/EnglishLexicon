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

    private var _wordsList = MutableLiveData<LinkedList<DataBaseEntry>>().apply {
        value = wordsList?.value
    }
    var wordsList: LiveData<LinkedList<DataBaseEntry>> = _wordsList

    fun getAllWordsFromDict(dictName: String): Single<LinkedList<DataBaseEntry>>
    {
         return repository.getAllFromTable(dictName)
    }

    fun deleteDict(dictName: String) : Single<Boolean>
    {
        return repository.dropTableFromDb(dictName)
    }

    fun getEntriesAndCounters(dictName: String, rowId: Int, order: String): Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        return repository.getEntriesAndCountersFromDb(dictName, rowId, order)
    }

    fun getRandomEntries(dictName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>
    {
        return repository.getRandomEntriesFromDb(dictName, rowId)
    }

    override fun onCleared()
    {
        composite.dispose()
        super.onCleared()
    }
}