package com.iti.careerpilot.login.presentation.otp

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.login.domain.model.AuthSession
import com.iti.careerpilot.login.domain.repository.AuthRepository
import com.iti.careerpilot.login.domain.usecase.SendOtpUseCase
import com.iti.careerpilot.login.domain.usecase.VerifyOtpUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OTPViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var sendOtpCallCount = 0
        var verifyOtpCallCount = 0
        var lastSentPhone: String? = null
        var lastVerifyPhone: String? = null
        var lastVerifyCode: String? = null

        var sendOtpResult: CareerPilotResult<Unit, NetworkError> = CareerPilotResult.Success(Unit)
        var verifyOtpResult: CareerPilotResult<AuthSession, NetworkError> = CareerPilotResult.Success(
            AuthSession(
                accessToken = "access_token",
                refreshToken = "refresh_token",
                expiresInMillis = 3600_000L,
                userId = 1L,
                username = "testuser",
                hasCompletedOnboarding = true
            )
        )

        override suspend fun sendOtp(phoneNumber: String): CareerPilotResult<Unit, NetworkError> {
            sendOtpCallCount++
            lastSentPhone = phoneNumber
            return sendOtpResult
        }

        override suspend fun verifyOtp(phoneNumber: String, code: String): CareerPilotResult<AuthSession, NetworkError> {
            verifyOtpCallCount++
            lastVerifyPhone = phoneNumber
            lastVerifyCode = code
            return verifyOtpResult
        }
    }

    private class FakeAccessRepository : AccessRepository {
        var refreshCallCount = 0
        var refreshResult: Result<Unit> = Result.success(Unit)

        private val _accessState = MutableStateFlow(AccessState.Free)
        override val accessState: StateFlow<AccessState> = _accessState.asStateFlow()

        override suspend fun refresh(): Result<Unit> {
            refreshCallCount++
            return refreshResult
        }

        override fun hasAccess(feature: FeatureKey): Boolean = true
        override suspend fun clear() {}
    }

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var accessRepository: FakeAccessRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        accessRepository = FakeAccessRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): OTPViewModel {
        return OTPViewModel(
            verifyOtp = VerifyOtpUseCase(authRepository),
            sendOtp = SendOtpUseCase(authRepository),
            refreshAccessUseCase = RefreshAccessUseCase(accessRepository),
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `initialize with phone number sets phone number in state and savedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)

        viewModel.onAction(OTPAction.PhoneNumberReceived("+201234567890"))
        testScheduler.advanceUntilIdle()

        assertEquals("+201234567890", viewModel.state.value.phoneNumber)
        assertEquals("+201234567890", savedStateHandle.get<String>("otp_phone_number"))
    }

    @Test
    fun `CodeChanged filters non-digits and triggers verify when reaching 6 digits`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("otp_phone_number" to "+201234567890")))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(OTPAction.CodeChanged("12a3"))
        assertEquals("123", viewModel.state.value.code)
        assertEquals(0, authRepository.verifyOtpCallCount)

        viewModel.onAction(OTPAction.CodeChanged("123456"))
        testScheduler.advanceUntilIdle()

        assertEquals("123456", viewModel.state.value.code)
        assertEquals(1, authRepository.verifyOtpCallCount)
        assertEquals("+201234567890", authRepository.lastVerifyPhone)
        assertEquals("123456", authRepository.lastVerifyCode)
    }

    @Test
    fun `verify success refreshes access and navigates to Home when onboarding is complete`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("otp_phone_number" to "+201234567890")))
        val emittedEvents = mutableListOf<OTPEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        viewModel.onAction(OTPAction.CodeChanged("123456"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, accessRepository.refreshCallCount)
        assertTrue(viewModel.state.value.isVerified)
        assertEquals(listOf(OTPEvent.NavigateToHome), emittedEvents)

        job.cancel()
    }

    @Test
    fun `verify success refreshes access and navigates to OnBoarding when onboarding is incomplete`() = runTest {
        authRepository.verifyOtpResult = CareerPilotResult.Success(
            AuthSession(
                accessToken = "access_token",
                refreshToken = "refresh_token",
                expiresInMillis = 3600_000L,
                userId = 1L,
                username = "testuser",
                hasCompletedOnboarding = false
            )
        )

        val viewModel = createViewModel(SavedStateHandle(mapOf("otp_phone_number" to "+201234567890")))
        val emittedEvents = mutableListOf<OTPEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        viewModel.onAction(OTPAction.CodeChanged("123456"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, accessRepository.refreshCallCount)
        assertTrue(viewModel.state.value.isVerified)
        assertEquals(listOf(OTPEvent.NavigateToOnBoarding), emittedEvents)

        job.cancel()
    }

    @Test
    fun `verify failure resets code and does not refresh access`() = runTest {
        authRepository.verifyOtpResult = CareerPilotResult.Error(NetworkError.SERVER)

        val viewModel = createViewModel(SavedStateHandle(mapOf("otp_phone_number" to "+201234567890")))
        viewModel.onAction(OTPAction.CodeChanged("123456"))
        testScheduler.advanceUntilIdle()

        assertEquals(0, accessRepository.refreshCallCount)
        assertFalse(viewModel.state.value.isVerified)
        assertFalse(viewModel.state.value.isVerifyingOtp)
        assertEquals("", viewModel.state.value.code)
    }

    @Test
    fun `ResendClicked sends OTP when canResend is true`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("otp_phone_number" to "+201234567890")))
        testScheduler.advanceTimeBy(61_000L) // Wait for countdown to reach 0
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.canResend)

        viewModel.onAction(OTPAction.ResendClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, authRepository.sendOtpCallCount)
        assertEquals("+201234567890", authRepository.lastSentPhone)
    }
}
