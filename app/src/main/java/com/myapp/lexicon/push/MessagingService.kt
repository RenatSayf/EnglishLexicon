package com.myapp.lexicon.push

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User

class MessagingService : FirebaseMessagingService() {

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    override fun onNewToken(token: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            saveMessagingTokenToCloud(currentUser.uid, token)
        }
        else {
            if (BuildConfig.DEBUG) {
                println("************ ${MessagingService::class.simpleName} currentUser is null **************************")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

    }

    private fun saveMessagingTokenToCloud(userId: String, token: String) {

        val map = mapOf(
            User.KEY_MESSAGING_TOKEN to token
        )
        db.collection(UserViewModel.COLLECTION_PATH)
            .document(userId)
            .update(map)
            .addOnSuccessListener {
                if (BuildConfig.DEBUG) {
                    println("********** Messaging token has been updated successful *********************")
                }
            }
            .addOnFailureListener {
                if (BuildConfig.DEBUG) {
                    println("********** Failure for token updating *********************")
                }
            }
    }
}