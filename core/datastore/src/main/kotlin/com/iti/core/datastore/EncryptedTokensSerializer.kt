package com.iti.core.datastore

import androidx.datastore.core.Serializer
import com.iti.core.datastore.encryption.KeystoreCryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class EncryptedTokensSerializer @Inject constructor() : Serializer<UserTokens> {

    override val defaultValue: UserTokens = UserTokens()

    override suspend fun readFrom(input: InputStream): UserTokens {
        val encryptedBytes = input.readBytes()
        if (encryptedBytes.isEmpty()) return defaultValue

        return try {
            val decryptedBytes = KeystoreCryptoManager.decrypt(encryptedBytes)
            Json.decodeFromString(decryptedBytes.decodeToString())
        } catch (_: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserTokens, output: OutputStream) {
        val jsonString = Json.encodeToString(t)
        val encryptedBytes = KeystoreCryptoManager.encrypt(jsonString.encodeToByteArray())
        withContext(Dispatchers.IO) {
            output.use { out ->
                out.write(encryptedBytes)
            }
        }
    }
}
