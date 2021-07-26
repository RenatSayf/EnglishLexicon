package com.myapp.lexicon.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditorSearchViewModel : ViewModel()
{
    private var _searchIsActive = MutableLiveData<Boolean>(false)
    @JvmField
    var searchIsActive: LiveData<Boolean> = _searchIsActive
    fun setSearchAsActive(value: Boolean)
    {
        _searchIsActive.value = value
    }

    private var _queryString = MutableLiveData("")
    @JvmField
    val queryString: LiveData<String> = _queryString
}