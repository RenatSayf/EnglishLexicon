package com.myapp.lexicon.repository.encrypt

import java.security.Key
import java.security.KeyPairGenerator
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

    override fun encodedData(data: String, pubKey: Key): Result<String> {

        val cipher = Cipher.getInstance("RSA").apply {
            init(Cipher.ENCRYPT_MODE, pubKey)
        }
        val bytes = try {
            cipher.doFinal(data.encodeToByteArray())
        } catch (e: Exception) {
            return Result.failure(Throwable(e))
        }
        return Result.success(bytes.toString())
    }
}