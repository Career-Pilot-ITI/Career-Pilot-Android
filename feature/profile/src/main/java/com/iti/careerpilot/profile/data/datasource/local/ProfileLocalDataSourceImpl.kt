package com.iti.careerpilot.profile.data.datasource.local

import android.util.Log
import androidx.datastore.core.DataStore
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

private const val ERROR_TAG = "CareerPilot: ProfileLocalDataSource"

class ProfileLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<UserProfile>
): ProfileLocalDataSource {

    override val userProfile: Flow<UserProfile> = dataStore.data

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        try {
            dataStore.updateData { currentData ->
                updateBlock(currentData)
            }
        } catch (e: IOException) {
            Log.e(ERROR_TAG, "Failed to update user profile: ${e.localizedMessage}", e)
        }
    }
}