package com.iti.careerpilot.features.settings.data.datastore

import androidx.datastore.core.Serializer
import com.iti.careerpilot.features.settings.domain.models.UserSettings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class DataStoreSerializer @Inject constructor() : Serializer<UserSettings> {

    override val defaultValue: UserSettings = UserSettings()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): UserSettings {
        return try {
            Json.decodeFromStream<UserSettings>(input)
        } catch (_: SerializationException) {
            defaultValue
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        Json.encodeToStream(t, output)
    }
}