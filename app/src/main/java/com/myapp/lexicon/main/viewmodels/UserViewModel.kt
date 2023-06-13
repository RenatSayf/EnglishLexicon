package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.getAdvertisingID
import com.myapp.lexicon.helpers.getCRC32CheckSum
import com.myapp.lexicon.models.User
import com.myapp.lexicon.settings.adsIsEnabled
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val COLLECTION_PATH = "users"


@HiltViewModel
class UserViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val db: FirebaseFirestore = Firebase.firestore

    private var _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    init {

        app.getAdvertisingID(
            onSuccess = {adId ->
                val userId = adId.getCRC32CheckSum().toString()
                checkUser(userId)
            },
            onUnavailable = {
                if (BuildConfig.DEBUG) {
                    Throwable("********** AdvertisingID is unavailable ***************").printStackTrace()
                }
            },
            onFailure = { err ->
                if (BuildConfig.DEBUG) {
                    Throwable("********** $err ***************").printStackTrace()
                }
            }
        )
    }

    fun insertOrUpdateUser(user: User) {
        val map = user.toHashMap()
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .set(map)
            .addOnSuccessListener {
                _user.value = user
            }
            .addOnFailureListener {
                it.printStackTrace()
                _user.value = null
            }
    }

    private fun checkUser(userId: String) {
        db.collection(COLLECTION_PATH)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data
                if (data != null) {
                    _user.value = User(document.id).apply {
                        this.currency = data["currency"].toString()
                        val reward = data["reward"].toString().ifEmpty {
                            0.0
                        }.toString().toDouble()
                        this.reward = reward
                    }
                }
                else {
                    val user = User(userId)
                    insertOrUpdateUser(user)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                _user.value = null
            }
    }


}