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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

private const val COLLECTION_PATH = "users"
private const val PERCENTAGE: Double = 0.7


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

    private fun addUser(user: User) {
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
                        this.currency = data[User.KEY_CURRENCY].toString()
                        val reward = data[User.KEY_REWARD].toString().ifEmpty {
                            0.0
                        }.toString().toDouble()
                        this.reward = reward
                    }
                }
                else {
                    val user = User(userId)
                    addUser(user)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                _user.value = null
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateUser(user: User) {

        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { snapshot ->
                val remoteUserData = snapshot.data as Map<String, String>
                user.reward = calculateReward(user, remoteUserData)

                db.collection(COLLECTION_PATH)
                    .document(user.id)
                    .set(user.toHashMap())
                    .addOnSuccessListener {
                        _user.value = user
                    }
                    .addOnFailureListener { t ->
                        if (BuildConfig.DEBUG) {
                            t.printStackTrace()
                        }
                    }
            }
            .addOnFailureListener { t ->
                if (BuildConfig.DEBUG) {
                    t.printStackTrace()
                }
            }
    }

    fun calculateReward(localUser: User, remoteUserData: Map<String, String?>): Double {
        val currentReward = try {
            remoteUserData[User.KEY_REWARD]?.ifEmpty {
                0.0
            }.toString().toDouble()
        } catch (e: Exception) {
            0.0
        }
        val newReward = currentReward + (localUser.reward * PERCENTAGE)
        return BigDecimal(newReward).setScale(3, RoundingMode.DOWN).toDouble()
    }


}