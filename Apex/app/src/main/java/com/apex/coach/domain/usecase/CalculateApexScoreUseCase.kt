package com.apex.coach.domain.usecase

import com.apex.coach.domain.model.ApexScore
import com.apex.coach.domain.model.ScoreComponents
import com.apex.coach.domain.model.WorkoutMode
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Apex Score Calculation
 * Formula from PRD Appendix A.1:
 * score = (hrv_normalized * 0.5) + (sleep_normalized * 0.3) + (rhr_normalized * 0.2)
 */
class CalculateApexScoreUseCase @Inject constructor() {
    
    operator fun invoke(hrv: Double, sleepHours: Double, rhr: Double): ApexScore {
        // Normalize HRV: (hrv / 100.0).coerceIn(0.0, 1.0) * 100
        val hrvNormalized = (hrv / 100.0).coerceIn(0.0, 1.0) * 100
        
        // Normalize Sleep: (sleepHours / 8.0).coerceIn(0.0, 1.0) * 100
        val sleepNormalized = (sleepHours / 8.0).coerceIn(0.0, 1.0) * 100
        
        // Normalize RHR: (1.0 - (rhr - 40.0) / 60.0).coerceIn(0.0, 1.0) * 100
        val rhrNormalized = (1.0 - (rhr - 40.0) / 60.0).coerceIn(0.0, 1.0) * 100
        
        // Calculate weighted score
        val score = (hrvNormalized * 0.5) + (sleepNormalized * 0.3) + (rhrNormalized * 0.2)
        
        // Determine mode based on PRD thresholds
        val mode = when {
            score > 70 -> WorkoutMode.FULL
            score >= 40 -> WorkoutMode.EXPRESS
            else -> WorkoutMode.REHAB
        }
        
        return ApexScore(
            value = score.roundToInt(),
            mode = mode,
            components = ScoreComponents(hrvNormalized, sleepNormalized, rhrNormalized)
        )
    }
}
