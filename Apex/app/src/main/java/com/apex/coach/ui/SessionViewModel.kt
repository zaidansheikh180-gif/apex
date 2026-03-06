package com.apex.coach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apex.coach.ml.pose.RepCounter
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionState(
    val exerciseName: String = "Squats",
    val repCount: Int = 0,
    val targetReps: Int = 12,
    val currentSet: Int = 1,
    val totalSets: Int = 3,
    val setProgress: Float = 0f,
    val sessionTonnage: Double = 0.0,
    val totalTonnage: Double = 0.0,
    val formScore: Double? = null,
    val completionTimeMinutes: Int = 0,
    val hasCameraPermission: Boolean = false,
    val currentPose: Pose? = null,
    val isBodyInFrame: Boolean = true,
    val isResting: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val lastRepWasCounted: Boolean = false,
    val formError: RepCounter.FormError? = null,
    val poorLightingDetected: Boolean = false,
    val isSessionComplete: Boolean = false,
    val showPaywall: Boolean = false,
    val isLastSet: Boolean = false
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repCounter: RepCounter
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var restTimerJob: kotlinx.coroutines.Job? = null

    fun onCameraPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasCameraPermission = granted) }
    }

    fun onPoseDetected(pose: Pose) {
        _state.update { it.copy(currentPose = pose, isBodyInFrame = true) }
        
        val result = repCounter.processPose(pose)
        
        _state.update {
            it.copy(
                repCount = result.count,
                lastRepWasCounted = result.triggerHaptic,
                formError = result.formError
            )
        }
        
        // Calculate progress
        val progress = result.count.toFloat() / it.targetReps
        _state.update { it.copy(setProgress = progress) }
        
        // Update tonnage (assuming 20kg per rep for now)
        val tonnagePerRep = 20.0
        val newTonnage = result.count * tonnagePerRep
        _state.update { it.copy(sessionTonnage = newTonnage) }
    }

    fun manualRepIncrement() {
        val newCount = _state.value.repCount + 1
        _state.update {
            it.copy(
                repCount = newCount,
                lastRepWasCounted = true,
                setProgress = newCount.toFloat() / it.targetReps,
                sessionTonnage = newCount * 20.0
            )
        }
    }

    fun completeSet() {
        val currentState = _state.value
        
        // Add to total tonnage
        _state.update {
            it.copy(
                totalTonnage = it.totalTonnage + it.sessionTonnage
            )
        }
        
        if (currentState.currentSet >= currentState.totalSets) {
            // Session complete
            _state.update {
                it.copy(
                    isSessionComplete = true,
                    completionTimeMinutes = 15 // Placeholder
                )
            }
        } else {
            // Start rest timer
            startRestTimer()
            _state.update {
                it.copy(
                    currentSet = it.currentSet + 1,
                    repCount = 0,
                    setProgress = 0f,
                    sessionTonnage = 0.0,
                    isLastSet = it.currentSet + 1 >= it.totalSets
                )
            }
        }
    }

    fun startRestTimer() {
        _state.update { it.copy(isResting = true, restSecondsRemaining = 60) }
        
        restTimerJob = viewModelScope.launch {
            var seconds = 60
            while (seconds > 0) {
                delay(1000)
                seconds--
                _state.update { it.copy(restSecondsRemaining = seconds) }
            }
            resumeFromRest()
        }
    }

    fun resumeFromRest() {
        restTimerJob?.cancel()
        _state.update { it.copy(isResting = false) }
    }

    fun skipExercise() {
        completeSet()
    }

    fun dismissFormError() {
        _state.update { it.copy(formError = null) }
    }

    fun dismissLightingWarning() {
        _state.update { it.copy(poorLightingDetected = false) }
    }

    fun switchToManualMode() {
        _state.update { it.copy(hasCameraPermission = false) }
    }

    fun initiatePurchase() {
        // Handle purchase flow
    }

    override fun onCleared() {
        super.onCleared()
        restTimerJob?.cancel()
    }
}
