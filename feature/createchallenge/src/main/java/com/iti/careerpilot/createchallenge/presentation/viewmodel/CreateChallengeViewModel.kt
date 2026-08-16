package com.iti.careerpilot.createchallenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeQuestion
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.VideoAnalysisConfig
import com.iti.careerpilot.createchallenge.R
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import com.iti.careerpilot.createchallenge.presentation.action.CreateChallengeAction
import com.iti.careerpilot.createchallenge.presentation.event.CreateChallengeEvent
import com.iti.careerpilot.createchallenge.presentation.state.CreateChallengeState
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val repository: CreateChallengeRepository,
    private val userProfileRepo: UserProfileRepo
) : ViewModel() {

    private val _state = MutableStateFlow(CreateChallengeState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateChallengeEvent>()
    val events = _events.receiveAsFlow()

    private var hasInitialized = false

    private fun initialize(challengeId: String?) {
        if (hasInitialized) return
        hasInitialized = true
        fetchTracks()
        challengeId?.let { id ->
            fetchExistingChallenge(id)
        }
    }

    private fun fetchTracks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTracks = true) }
            repository.getTracks()
                .onSuccess { tracks ->
                    val tracksList = if (tracks.isEmpty()) getFakeTracks() else tracks
                    _state.update { it.copy(tracks = tracksList.toImmutableList(), isLoadingTracks = false) }
                    if (_state.value.selectedTrack == null) {
                        _state.update { it.copy(selectedTrack = tracksList.firstOrNull()) }
                    }
                }
                .onError { error ->
                    val fakeTracks = getFakeTracks()
                    _state.update { it.copy(tracks = fakeTracks.toImmutableList(), isLoadingTracks = false) }
                    if (_state.value.selectedTrack == null) {
                        _state.update { it.copy(selectedTrack = fakeTracks.firstOrNull()) }
                    }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    private fun fetchExistingChallenge(challengeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isEditMode = true, existingChallengeId = challengeId) }
            repository.getChallenge(challengeId)
                .onSuccess { challenge ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            selectedTrack = it.tracks.find { t -> t.id == challenge.trackId },
                            visibility = challenge.visibility,
                            seniorityLevel = challenge.seniorityLevel,
                            challengeType = challenge.type,
                            analyzePosture = challenge.videoAnalysisConfig?.analyzePosture ?: false,
                            analyzeHands = challenge.videoAnalysisConfig?.analyzeHands ?: false,
                            questions = challenge.questions.map { q -> q.text }.toImmutableList()
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    private fun getFakeTracks() = listOf(
        Track(1, "Android Developer"),
        Track(2, "Frontend Developer"),
        Track(3, "Backend Developer"),
        Track(4, "Data Scientist"),
        Track(5, "UI/UX Designer")
    )

    fun onAction(action: CreateChallengeAction) {
        when (action) {
            is CreateChallengeAction.Initial -> initialize(action.challengeId)
            is CreateChallengeAction.OnTrackSelected -> _state.update { it.copy(selectedTrack = action.track) }
            is CreateChallengeAction.OnVisibilityChanged -> _state.update { it.copy(visibility = action.visibility) }
            is CreateChallengeAction.OnSeniorityLevelChanged -> _state.update { it.copy(seniorityLevel = action.level) }
            is CreateChallengeAction.OnChallengeTypeChanged -> _state.update { it.copy(challengeType = action.type) }
            is CreateChallengeAction.OnPostureToggle -> _state.update { it.copy(analyzePosture = action.enabled) }
            is CreateChallengeAction.OnHandsToggle -> _state.update { it.copy(analyzeHands = action.enabled) }
            is CreateChallengeAction.OnQuestionTextChange -> {
                val newQuestions = _state.value.questions.toMutableList()
                newQuestions[action.index] = action.text
                _state.update { it.copy(questions = newQuestions.toImmutableList()) }
            }
            CreateChallengeAction.OnAddQuestion -> {
                _state.update { it.copy(questions = (it.questions + "").toImmutableList()) }
            }
            is CreateChallengeAction.OnRemoveQuestion -> {
                val questionText = _state.value.questions.getOrNull(action.index)
                if (questionText.isNullOrBlank()) {
                    removeQuestion(action.index)
                } else {
                    _state.update { it.copy(questionToDeleteIndex = action.index) }
                }
            }
            CreateChallengeAction.OnConfirmDeleteQuestion -> {
                _state.value.questionToDeleteIndex?.let { removeQuestion(it) }
                _state.update { it.copy(questionToDeleteIndex = null) }
            }
            CreateChallengeAction.OnDismissDeleteConfirmation -> _state.update { it.copy(questionToDeleteIndex = null) }
            CreateChallengeAction.OnSubmit -> submitChallenge()
            CreateChallengeAction.OnDismissError -> _state.update { it.copy(error = null) }
            CreateChallengeAction.OnDismissSuccess -> {
                _state.update { it.copy(isSuccessDialogVisible = false, invitationCode = null) }
                viewModelScope.launch { _events.send(CreateChallengeEvent.NavigateToDashboard) }
            }
            CreateChallengeAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(CreateChallengeEvent.NavigateBack) }
            }
        }
    }

    private fun removeQuestion(index: Int) {
        if (_state.value.questions.size > 10) {
            val newQuestions = _state.value.questions.toMutableList()
            newQuestions.removeAt(index)
            _state.update { it.copy(questions = newQuestions.toImmutableList()) }
        }
    }

    private fun submitChallenge() {
        val current = _state.value
        val nonEmptyQuestions = current.questions.filter { it.isNotBlank() }

        if (nonEmptyQuestions.size < 10) {
            viewModelScope.launch {
                CareerPilotSnackbarController.show(UIText.StringResource(R.string.create_challenge_error_min_questions))
            }
            return
        }

        // Duplicate check
        if (nonEmptyQuestions.distinct().size != nonEmptyQuestions.size) {
            viewModelScope.launch {
                CareerPilotSnackbarController.show(UIText.StringResource(R.string.create_challenge_error_duplicate_questions))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            repository.validateQuestions(nonEmptyQuestions)
                .onSuccess {
                    val userProfile = userProfileRepo.readUserProfile()
                    val challengeId = current.existingChallengeId ?: repository.generateChallengeId(current.visibility)
                    val challenge = Challenge(
                        id = challengeId,
                        creatorId = userProfile.id,
                        creatorUsername = userProfile.account.username,
                        creatorName = userProfile.personal.displayName,
                        trackId = current.selectedTrack?.id ?: 0L,
                        trackName = current.selectedTrack?.name ?: "",
                        visibility = current.visibility,
                        seniorityLevel = current.seniorityLevel,
                        creationDate = System.currentTimeMillis(),
                        invitationCode = challengeId,
                        questions = nonEmptyQuestions.map {
                            ChallengeQuestion(
                                id = UUID.randomUUID().toString(), text = it
                            )
                        },
                        type = current.challengeType,
                        videoAnalysisConfig = if (current.challengeType == ChallengeType.VIDEO_AND_AUDIO) {
                            VideoAnalysisConfig(
                                analyzeFace = true,
                                analyzePosture = current.analyzePosture,
                                analyzeHands = current.analyzeHands
                            )
                        } else null
                    )

                    repository.createChallenge(challenge)
                        .onSuccess {
                            _state.update { it.copy(isSubmitting = false, isSuccessDialogVisible = true, invitationCode = challengeId) }
                        }
                        .onError { _ ->
                            _state.update { it.copy(isSubmitting = false) }
                            CareerPilotSnackbarController.show(UIText.StringResource(R.string.create_challenge_error_save))
                        }
                }
                .onError { _ ->
                    _state.update { it.copy(isSubmitting = false) }
                    CareerPilotSnackbarController.show(UIText.StringResource(R.string.create_challenge_error_ai_validation))
                }
        }
    }
}
