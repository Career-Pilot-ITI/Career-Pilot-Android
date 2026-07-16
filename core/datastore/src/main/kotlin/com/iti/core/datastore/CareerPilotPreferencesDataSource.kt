package com.iti.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CareerPilotPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val hasCompletedOnboarding: Flow<Boolean> =
        userPreferences.data.map { it.hasCompletedOnboarding }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        userPreferences.updateData { it.copy(hasCompletedOnboarding = completed) }
    }
}
