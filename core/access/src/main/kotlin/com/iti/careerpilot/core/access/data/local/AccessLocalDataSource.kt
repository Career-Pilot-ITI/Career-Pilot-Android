package com.iti.careerpilot.core.access.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iti.careerpilot.core.access.di.AccessDataStore
import com.iti.core.model.AccessState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessLocalDataSource @Inject constructor(
    @AccessDataStore private val dataStore: DataStore<Preferences>
) {
    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    val accessStateFlow: Flow<AccessState> = dataStore.data
        .map { preferences ->
            val jsonString = preferences[ACCESS_STATE_KEY]
            if (jsonString.isNullOrBlank()) {
                AccessState.Free
            } else {
                runCatching {
                    json.decodeFromString<AccessState>(jsonString)
                }.getOrDefault(AccessState.Free)
            }
        }
        .catch {
            emit(AccessState.Free)
        }

    suspend fun save(state: AccessState) {
        val jsonString = json.encodeToString(state)
        dataStore.edit { preferences ->
            preferences[ACCESS_STATE_KEY] = jsonString
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_STATE_KEY)
        }
    }

    companion object {
        private val ACCESS_STATE_KEY = stringPreferencesKey("access_state_json")
    }
}
