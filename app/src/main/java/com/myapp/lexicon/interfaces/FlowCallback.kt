package com.myapp.lexicon.interfaces

import com.myapp.lexicon.main.viewmodels.UserViewModel

interface FlowCallback {
    fun onStart() = Unit
    fun onCompletion(thr: Throwable?) = Unit
    fun onResult(result: UserViewModel.State?)
}