package com.myapp.lexicon.repository.encrypt

import java.security.Key

interface IEncrypt {

    fun getKeyPair(): Result<Pair<Key, Key>>

    fun encodedData(data: String, pubKey: String): Result<String>
}