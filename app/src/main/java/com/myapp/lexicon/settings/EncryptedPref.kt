package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.myapp.lexicon.models.Tokens

private const val ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
private const val REFRESH_TOKEN = "KEY_REFRESH_TOKEN"
private const val FILE_NAME = "lexicon_secure_prefs"


private val Context.encryptPref: SharedPreferences
    get() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            this,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

fun Context.saveAuthTokens(tokens: Tokens) {
    this.encryptPref.edit().apply {
        putString(ACCESS_TOKEN, tokens.accessToken)
        putString(REFRESH_TOKEN, tokens.refreshToken)
    }.apply()
}

val Context.accessToken: String
    get() {
        return this.encryptPref.getString(ACCESS_TOKEN, "").toString()
    }

val Context.refreshToken: String
    get() {
        return this.encryptPref.getString(REFRESH_TOKEN, "").toString()
    }