package com.myapp.lexicon.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.helpers.PlayListBus
import com.myapp.lexicon.repository.DataRepositoryImpl
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel @ViewModelInject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    private val composite = CompositeDisposable()

    private var _currentDict = MutableLiveData<String>()
    var currentDict : LiveData<String> = _currentDict

    fun getDictList() : Observable<MutableList<String>>
    {
        return repository.getTableListFromDb()
    }


    private var _playList = MutableLiveData<MutableList<String>>()
    var playList : LiveData<MutableList<String>> = _playList

    private fun setPlayList()
    {
        _playList.value = repository.getTableListFromSettings()
    }

    private var _wordsList = MutableLiveData<MutableList<Word>>()
    var wordsList: LiveData<MutableList<Word>> = _wordsList

    fun getWordsFromDict(dictName: String, id: Int, limit: Int): Single<MutableList<Word>>
    {
         return repository.getEntriesFromDbByDictName(dictName, id, limit)
    }

    fun deleteDict(dictName: String) : Single<Boolean>
    {
        return repository.dropTableFromDb(dictName)
    }

    fun getEntriesAndCounters(dictName: String, rowId: Int, order: String, limit: Int = 2): Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        return repository.getEntriesAndCountersFromDb(dictName, rowId, order, limit)
    }

    fun getRandomEntries(dictName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>
    {
        return repository.getRandomEntriesFromDb(dictName, rowId)
    }

    init
    {
        _playList.value = repository.getTableListFromSettings() as ArrayList<String>
        _currentDict.value = repository.getCurrentWordFromSettings().dictName

        val dictName = _currentDict.value!!
//        composite.add(getEntriesAndCounters(dictName, 1, "ASC", 10000)
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ pair ->
//                    if (pair.first.isNotEmpty() && pair.second.isNotEmpty())
//                    {
//                        _wordsList.value = pair
//                        _currentDict.value = pair.second[0].dictName
//                    }
//                }, { throwable -> throwable.printStackTrace() }))

        composite.add(getWordsFromDict(dictName, 1, Int.MAX_VALUE)
                .observeOn(Schedulers.computation())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    _wordsList.value = list
                }, { t ->
                    t.printStackTrace()
                }))
    }

    override fun onCleared()
    {

        composite.dispose()
        composite.clear()
        super.onCleared()
    }
}