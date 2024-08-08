package com.myapp.lexicon.repository.encrypt

import android.util.Base64
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


class Encrypt : IEncrypt {
    override fun getKeyPair(): Result<Pair<Key, Key>> {
        try {
            val generator = KeyPairGenerator.getInstance("RSA").apply {
                initialize(1024)
            }
            val keyPair = generator.genKeyPair()
            return Result.success(Pair(first = keyPair.public, second = keyPair.private))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun encodedData(data: String, pubKey: String): Result<String> {

        val keyBytes = Base64.decode(pubKey, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(spec)

        val cipher = Cipher.getInstance("RSA").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }
        return try {
            val bytes = cipher.doFinal(data.encodeToByteArray())
            val encodeString = Base64.encodeToString(bytes, Base64.DEFAULT)
            Result.success(encodeString)
        } catch (e: Exception) {
            Result.failure(Throwable(e))
        }
    }
}