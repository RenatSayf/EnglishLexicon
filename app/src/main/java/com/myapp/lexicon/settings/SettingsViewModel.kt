package com.myapp.lexicon.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    private var _storagePrefInitValue = app.cloudStorageEnabled
    val storagePrefHasChanged: LiveData<Boolean>
        get() {
            return MutableLiveData(_storagePrefInitValue != app.cloudStorageEnabled)
        }

}