package com.myapp.lexicon.auth

import com.myapp.lexicon.models.User

interface AuthListener {

    fun refreshAuthState(user: User)
}