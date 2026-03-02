package com.apex.coach.domain.model

import kotlin.math.roundToInt

data class ApexScore(
    val value: Int,
    val mode: WorkoutMode,
    val components: ScoreComponents,
    val calculationTimestamp: Long = System.currentTimeMillis()
)

data class ScoreComponents(
    val hrvNormalized: Double,
    val sleepNormalized: Double,
    val rhrNormalized: Double
)

enum class WorkoutMode {
    FULL,       // Score > 70
    EXPRESS,    // Score 40-70
    REHAB       // Score < 40
}
