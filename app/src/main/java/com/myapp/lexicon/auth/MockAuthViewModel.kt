package com.myapp.lexicon.auth

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MockAuthViewModel @Inject constructor(
    app: Application
): AuthViewModel(app) {
}