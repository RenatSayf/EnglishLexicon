@file:Suppress("UnnecessaryVariable")

package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    companion object {

        private const val COLLECTION_PATH = "users"
        val USER_PERCENTAGE: Double = Firebase.remoteConfig.getDouble("USER_PERCENTAGE")
        val REVENUE_RATIO: Double = Firebase.remoteConfig.getDouble("REVENUE_RATIO")
    }

    private val db: FirebaseFirestore = Firebase.firestore

    private var _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    fun setUser(user: User) {
        _user.value = user
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

    fun addUserIfNotExists(user: User) {
        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data
                if (data != null) {
                    _user.value = User(document.id).apply {
                        this.currency = data[User.KEY_CURRENCY].toString()
                        val reward = data[User.KEY_USER_REWARD].toString().ifEmpty {
                            0.0
                        }.toString().toDouble()
                        this.userReward = reward
                    }
                }
                else {
                    val newUser = User(user.id)
                    addUser(newUser)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                _user.value = null
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateUser(revenuePerAd: Double, user: User) {

        db.collection(COLLECTION_PATH)
            .document(user.id)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.data != null) {
                    val remoteUserData = snapshot.data as Map<String, String>
                    user.reallyRevenue = calculateReallyRevenue(revenuePerAd, remoteUserData)
                    user.userReward = calculateUserReward(revenuePerAd, remoteUserData)
                    user.totalRevenue = calculateTotalRevenue(revenuePerAd, remoteUserData)

                    if (user.reallyRevenue > 0 && user.userReward > 0) {
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
                    } else {
                        if (BuildConfig.DEBUG) {
                            val message =
                                "******************** A negative revenue value cannot be sent: ${user.reallyRevenue}, ${user.userReward} ************"
                            Throwable(message).printStackTrace()
                        }
                    }
                }
            }
            .addOnFailureListener { t ->
                if (BuildConfig.DEBUG) {
                    t.printStackTrace()
                }
            }
    }
    fun calculateReallyRevenue(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentRevenue = try {
            remoteUserData[User.KEY_REALLY_REVENUE]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentRevenue < 0) {
            currentRevenue
        } else {
            val newRevenue = currentRevenue + (revenuePerAd * REVENUE_RATIO)
            newRevenue
        }
    }

    fun calculateUserReward(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentReward = try {
            remoteUserData[User.KEY_USER_REWARD]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentReward < 0) {
            currentReward
        } else {
            val newReward = currentReward + ((revenuePerAd * REVENUE_RATIO) * USER_PERCENTAGE)
            newReward
        }
    }

    fun calculateTotalRevenue(revenuePerAd: Double, remoteUserData: Map<String, String?>): Double {
        val currentRevenue = try {
            remoteUserData[User.KEY_TOTAL_REVENUE]?.ifEmpty {
                -1.0
            }.toString().toDouble()
        } catch (e: Exception) {
            -1.0
        }
        return if (currentRevenue < 0) {
            currentRevenue
        } else {
            val newRevenue = currentRevenue + revenuePerAd
            newRevenue
        }
    }



}