package com.myapp.lexicon.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.parse.ParseUser

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            saveMessagingTokenToCloud(token)
        }
        else {
            if (BuildConfig.DEBUG) {
                println("************ ${MessagingService::class.simpleName} currentUser is null **************************")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

    }

    private fun saveMessagingTokenToCloud(token: String) {

        val map = mapOf(
            User.KEY_MESSAGING_TOKEN to token
        )
        UserViewModel(this.application).updateUserDataIntoCloud(map)
    }
}