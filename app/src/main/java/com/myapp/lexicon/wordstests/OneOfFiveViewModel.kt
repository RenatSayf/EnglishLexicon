package com.myapp.lexicon.wordstests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OneOfFiveViewModel : ViewModel()
{
    private var _instance: MutableLiveData<OneOfFiveFragmNew> = MutableLiveData()
    var instance: LiveData<OneOfFiveFragmNew>? = _instance
    fun setInstance(instance: OneOfFiveFragmNew)
    {
        _instance.value = instance
    }

}