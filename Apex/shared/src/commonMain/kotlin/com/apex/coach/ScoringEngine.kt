package com.apex.coach

import kotlinx.serialization.Serializable

@Serializable
data class ApexScore(val score: Int, val trend: String)

/**
 * Core Scoring Engine (PRD 2.1)
 * Shared logic across iOS and Android
 */
class ScoringEngine {
    fun calculateApexScore(hrvDelta: Double, sleepScore: Double, rhrStability: Double): ApexScore {
        // Implementation of the Apex Score formula defined in the PRD
        val rawScore = (hrvDelta * 0.4 + sleepScore * 0.4 + rhrStability * 0.2).toInt()
        val score = rawScore.coerceIn(0, 100)
        
        val trend = when {
            score >= 75 -> "Peak Performance"
            score >= 40 -> "Express Mode Recommended"
            else -> "Active Recovery"
        }
        
        return ApexScore(score, trend)
    }
}
