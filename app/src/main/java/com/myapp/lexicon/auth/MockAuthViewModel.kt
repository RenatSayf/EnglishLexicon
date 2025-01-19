package com.myapp.lexicon.auth

import com.myapp.lexicon.di.INetRepositoryModule
import javax.inject.Inject


class MockAuthViewModel @Inject constructor(netModule: INetRepositoryModule): AuthViewModel(netModule) {

}