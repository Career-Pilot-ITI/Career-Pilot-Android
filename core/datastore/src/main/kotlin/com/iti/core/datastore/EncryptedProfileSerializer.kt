package com.iti.core.datastore

import androidx.datastore.core.Serializer
import com.iti.core.datastore.encryption.KeystoreCryptoManager
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class EncryptedProfileSerializer @Inject constructor() : Serializer<UserProfile> {

    override val defaultValue: UserProfile = UserProfile()

    override suspend fun readFrom(input: InputStream): UserProfile {
        val encryptedBytes = input.readBytes()
        if (encryptedBytes.isEmpty()) return defaultValue

        return try {
            val decryptedBytes = KeystoreCryptoManager.decrypt(encryptedBytes)
            Json.decodeFromString(decryptedBytes.decodeToString())
        } catch (_: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserProfile, output: OutputStream) {
        val jsonString = Json.encodeToString(t)
        val encryptedBytes = KeystoreCryptoManager.encrypt(jsonString.encodeToByteArray())
        withContext(Dispatchers.IO) {
            output.use { out ->
                out.write(encryptedBytes)
            }
        }
    }
}
