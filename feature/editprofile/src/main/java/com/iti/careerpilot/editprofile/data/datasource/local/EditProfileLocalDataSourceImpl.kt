package com.iti.careerpilot.editprofile.data.datasource.local

import android.util.Log
import androidx.datastore.core.DataStore
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

private const val ERROR_TAG = "CareerPilot: EditProfileLocalDataSource"

class EditProfileLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<UserProfile>
): EditProfileLocalDataSource {

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