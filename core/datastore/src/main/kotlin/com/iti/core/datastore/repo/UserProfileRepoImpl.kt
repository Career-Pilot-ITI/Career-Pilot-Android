package com.iti.core.datastore.repo

import android.util.Log
import androidx.datastore.core.DataStore
import com.iti.common.dispatcher.di.ApplicationScope
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.io.IOException
import javax.inject.Inject

private const val ERROR_TAG = "CareerPilot: UserProfileRepo"

class UserProfileRepoImpl @Inject constructor(
    @ApplicationScope scope: CoroutineScope,
    private val dataStore: DataStore<UserProfile>
): UserProfileRepo {

    override val userProfile: StateFlow<UserProfile> = dataStore.data
        .stateIn(
            scope, SharingStarted.Eagerly, UserProfile()
        )

    override suspend fun readUserProfile(): UserProfile = dataStore.data.first()

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        try {
            dataStore.updateData { currentData ->
                updateBlock(currentData)
            }
        } catch (e: IOException) {
            Log.e(ERROR_TAG, "Failed to update user profile: ${e.localizedMessage}", e)
        }
    }

    override suspend fun clearUserProfile() {
        try {
            dataStore.updateData {
                UserProfile()
            }
        } catch (e: IOException) {
            Log.e(ERROR_TAG, "Failed to clear user profile: ${e.localizedMessage}", e)
        }
    }
}
