package com.iti.careerpilot.core.access.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.iti.careerpilot.core.access.data.local.AccessLocalDataSource
import com.iti.careerpilot.core.access.data.remote.AccessRemoteDataSource
import com.iti.careerpilot.core.access.data.remote.dto.SubscriptionStatusDto
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AccessRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeUserProfileRepo(initial: UserProfile = UserProfile()) : UserProfileRepo {
        private val _userProfile = MutableStateFlow(initial)
        override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()
        override suspend fun readUserProfile(): UserProfile = _userProfile.value
        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.update(updateBlock)
        }
        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }
        override suspend fun setBodyLanguageConsent(given: Boolean) {}
    }

    private class TestAccessRemoteDataSource(
        var statusDto: SubscriptionStatusDto = SubscriptionStatusDto(),
        var balance: Int = 0
    ) : AccessRemoteDataSource {
        override suspend fun getSubscriptionStatus(): SubscriptionStatusDto = statusDto
        override suspend fun getWalletBalance(): Int = balance
    }

    @Test
    fun refresh_savesRemoteStateAndSyncsToUserProfile() = runTest(testDispatcher) {
        val testFile = tmpFolder.newFile("test_access_1.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val localDataSource = AccessLocalDataSource(dataStore)
        val userProfileRepo = FakeUserProfileRepo()

        val remote = TestAccessRemoteDataSource(
            statusDto = SubscriptionStatusDto(
                tier = "PLUS",
                isActive = true,
                coinBalance = 150
            ),
            balance = 150
        )

        val repository = AccessRepositoryImpl(
            remote = remote,
            local = localDataSource,
            userProfileRepo = userProfileRepo,
            ioDispatcher = testDispatcher,
            scope = backgroundScope
        )

        val result = repository.refresh()
        assertTrue(result.isSuccess)
        assertEquals(Plan.PLUS, repository.accessState.value.plan)
        assertEquals(150, repository.accessState.value.coinBalance)
        assertEquals(150, userProfileRepo.readUserProfile().account.coinBalance)
        assertEquals("PLUS", userProfileRepo.readUserProfile().account.subscriptionTier)
    }

    @Test
    fun hasAccess_returnsCorrectAccessForFeature() = runTest(testDispatcher) {
        val testFile = tmpFolder.newFile("test_access_3.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val localDataSource = AccessLocalDataSource(dataStore)
        val userProfileRepo = FakeUserProfileRepo()

        val state = AccessState(
            plan = Plan.MAX,
            features = setOf(FeatureKey.VoicePracticeMode, FeatureKey.CvAiAnalysis),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = null,
            coinBalance = 50
        )
        localDataSource.save(state)

        val repository = AccessRepositoryImpl(
            remote = TestAccessRemoteDataSource(),
            local = localDataSource,
            userProfileRepo = userProfileRepo,
            ioDispatcher = testDispatcher,
            scope = backgroundScope
        )

        assertTrue(repository.hasAccess(FeatureKey.VoicePracticeMode))
        assertFalse(repository.hasAccess(FeatureKey.AdvancedReports))
    }

    @Test
    fun clear_resetsLocalAndUserProfile() = runTest(testDispatcher) {
        val testFile = tmpFolder.newFile("test_access_4.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val localDataSource = AccessLocalDataSource(dataStore)
        val userProfileRepo = FakeUserProfileRepo()

        val state = AccessState(
            plan = Plan.MAX,
            features = setOf(FeatureKey.VoicePracticeMode),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = null,
            coinBalance = 50
        )
        localDataSource.save(state)

        val repository = AccessRepositoryImpl(
            remote = TestAccessRemoteDataSource(),
            local = localDataSource,
            userProfileRepo = userProfileRepo,
            ioDispatcher = testDispatcher,
            scope = backgroundScope
        )

        repository.clear()

        assertEquals(AccessState.Free, repository.accessState.value)
        assertEquals("", userProfileRepo.readUserProfile().account.subscriptionTier)
        assertEquals(0, userProfileRepo.readUserProfile().account.coinBalance)
    }

    @Test
    fun `backend tier string PLUS maps to Plan PLUS with correct features`() {
        val dto = SubscriptionStatusDto(
            tier = "PLUS",
            isActive = true,
            renewalDate = "2026-09-15T12:00:00"
        )
        val domain = dto.toDomain()
        assertEquals(Plan.PLUS, domain.plan)
        assertTrue(FeatureKey.AtsFeatures in domain.features)
        assertTrue(FeatureKey.CvAiAnalysis in domain.features)
        assertFalse(FeatureKey.VideoInterview in domain.features)
        assertTrue(FeatureKey.Quizzes in domain.features)
    }

    @Test
    fun `backend plan string PRO maps to Plan MAX`() {
        val dto = SubscriptionStatusDto(
            plan = "PRO", features = emptyList(), quotas = emptyList(), coinBalance = 0
        )
        assertEquals(Plan.MAX, dto.toDomain().plan)
    }

    @Test
    fun `backend plan string pro lowercase maps to Plan MAX`() {
        val dto = SubscriptionStatusDto(
            plan = "pro", features = emptyList(), quotas = emptyList(), coinBalance = 0
        )
        assertEquals(Plan.MAX, dto.toDomain().plan)
    }

    @Test
    fun `unknown backend plan string falls back to FREE`() {
        val dto = SubscriptionStatusDto(
            plan = "ENTERPRISE", features = emptyList(), quotas = emptyList(), coinBalance = 0
        )
        assertEquals(Plan.FREE, dto.toDomain().plan)
    }
}
