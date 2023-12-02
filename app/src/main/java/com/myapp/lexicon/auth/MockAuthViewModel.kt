package com.myapp.lexicon.auth

import android.app.Application
import javax.inject.Inject


class MockAuthViewModel @Inject constructor(
    app: Application
): AuthViewModel(app) {
}