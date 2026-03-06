package com.apex.coach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apex.coach.data.health.HealthConnectManager
import com.apex.coach.domain.model.ApexScore
import com.apex.coach.domain.model.Workout
import com.apex.coach.domain.model.WorkoutMode
import com.apex.coach.domain.usecase.CalculateApexScoreUseCase
import com.apex.coach.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val apexScore: ApexScore? = null,
    val isLoadingScore: Boolean = false,
    val selectedMode: WorkoutMode = WorkoutMode.FULL,
    val streakDays: Int = 0,
    val last14Days: List<CalculateStreakUseCase.DayStatus> = emptyList(),
    val isStreakFrozen: Boolean = false,
    val hrv: Double? = null,
    val sleepHours: Double? = null,
    val rhr: Double? = null,
    val isDataStale: Boolean = false,
    val lastSyncTime: String? = null,
    val todayWorkout: Workout? = null,
    val healthConnectUnavailable: Boolean = false,
    val permissionsDenied: Boolean = false,
    val manualEnergyLevel: Int = 3
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateApexScoreUseCase: CalculateApexScoreUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        refreshScore()
        loadStreakData()
    }

    fun refreshScore() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingScore = true) }
            
            try {
                val biometrics = healthConnectManager.readLatestBiometrics()
                
                if (biometrics != null) {
                    val apexScore = calculateApexScoreUseCase(
                        hrv = biometrics.hrv,
                        sleepHours = biometrics.sleepHours,
                        rhr = biometrics.rhr
                    )
                    
                    _state.update {
                        it.copy(
                            apexScore = apexScore,
                            hrv = biometrics.hrv,
                            sleepHours = biometrics.sleepHours,
                            rhr = biometrics.rhr,
                            isLoadingScore = false,
                            isDataStale = false,
                            lastSyncTime = "Just now"
                        )
                    }
                } else {
                    val defaultScore = calculateApexScoreUseCase(
                        hrv = 50.0,
                        sleepHours = 7.0,
                        rhr = 60.0
                    )
                    
                    _state.update {
                        it.copy(
                            apexScore = defaultScore,
                            isLoadingScore = false,
                            isDataStale = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingScore = false, isDataStale = true) }
            }
        }
    }

    private fun loadStreakData() {
        viewModelScope.launch {
            val streakInfo = calculateStreakUseCase.getStreakInfo()
            _state.update {
                it.copy(
                    streakDays = streakInfo.currentStreak,
                    last14Days = streakInfo.last14Days,
                    isStreakFrozen = streakInfo.isFrozen
                )
            }
        }
    }

    fun onModeSelected(mode: WorkoutMode) {
        _state.update { it.copy(selectedMode = mode) }
    }

    fun syncHealthData() {
        refreshScore()
    }

    fun openHealthConnectInstall() {
        // Opens Health Connect install intent
    }

    fun onManualEnergyChange(level: Int) {
        _state.update { it.copy(manualEnergyLevel = level) }
    }
}
