package com.iti.core.datastore.settings.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import com.iti.core.datastore.settings.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

const val ERROR_TAG = "UserSettingsRepo"

class UserSettingsRepoImp @Inject constructor(
    private val dataStore: DataStore<UserSettings>,
) : UserSettingsRepo {

    override val settingsFlow: Flow<UserSettings> = dataStore.data

    override suspend fun updateUserSettings(updateBlock: (UserSettings) -> UserSettings) {
        try {
            dataStore.updateData { currentData ->
                updateBlock(currentData)
            }
        } catch (e: IOException) {
            Log.e(ERROR_TAG, "Failed to update user settings: ${e.localizedMessage}", e)
        }
    }
}