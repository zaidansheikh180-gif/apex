package com.apex.coach.ml.pose

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Rep Counter State Machine
 * From PRD Appendix A.2:
 * States: IDLE → GOING_DOWN → BOTTOM → COMING_UP → TOP (+1 rep)
 */
@Singleton
class RepCounter @Inject constructor() {
    
    enum class RepState {
        IDLE,
        GOING_DOWN,
        BOTTOM,
        COMING_UP,
        TOP
    }
    
    data class RepResult(
        val count: Int,
        val state: RepState,
        val triggerHaptic: Boolean = false,
        val formError: FormError? = null
    )
    
    enum class FormError {
        SQUAT_DEPTH,
        KNEE_VALGUS
    }
    
    private var currentState = RepState.IDLE
    private var repCount = 0
    private var lastRepTime = 0L
    private val debounceMs = 500L
    
    // Tracking variables for squat depth
    private var startHipY = 0f
    private var minHipY = Float.MAX_VALUE
    private var previousHipY = 0f
    
    fun reset() {
        currentState = RepState.IDLE
        repCount = 0
        lastRepTime = 0L
        startHipY = 0f
        minHipY = Float.MAX_VALUE
        previousHipY = 0f
    }
    
    fun processPose(pose: Pose, exerciseType: ExerciseType = ExerciseType.SQUAT): RepResult {
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        
        if (hip == null || knee == null || ankle == null) {
            return RepResult(repCount, currentState, triggerHaptic = false)
        }
        
        val currentHipY = hip.position.y
        
        if (startHipY == 0f) {
            startHipY = currentHipY
        }
        
        // Update min hip position
        if (currentHipY < minHipY) {
            minHipY = currentHipY
        }
        
        // State machine
        val newState = when (currentState) {
            RepState.IDLE -> {
                if (currentHipY < previousHipY - 5) RepState.GOING_DOWN else RepState.IDLE
            }
            RepState.GOING_DOWN -> {
                if (currentHipY < minHipY * 0.9) {
                    RepState.BOTTOM
                } else {
                    RepState.GOING_DOWN
                }
            }
            RepState.BOTTOM -> {
                if (currentHipY > previousHipY + 5) RepState.COMING_UP else RepState.BOTTOM
            }
            RepState.COMING_UP -> {
                if (currentHipY > startHipY * 0.95) {
                    val now = System.currentTimeMillis()
                    if (now - lastRepTime > debounceMs) {
                        lastRepTime = now
                        repCount++
                        RepState.TOP
                    } else {
                        RepState.COMING_UP
                    }
                } else {
                    RepState.COMING_UP
                }
            }
            RepState.TOP -> {
                // Reset for next rep
                startHipY = currentHipY
                minHipY = Float.MAX_VALUE
                RepState.IDLE
            }
        }
        
        val triggerHaptic = newState == RepState.TOP && currentState != RepState.TOP
        
        // Check for form errors
        val formError = if (currentState == RepState.GOING_DOWN) {
            checkSquatDepth(pose)
        } else null
        
        currentState = newState
        previousHipY = currentHipY
        
        return RepResult(repCount, currentState, triggerHaptic, formError)
    }
    
    private fun checkSquatDepth(pose: Pose): FormError? {
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position
        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)?.position
        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)?.position
        
        if (hip == null || knee == null || ankle == null) return null
        
        val angle = calculateAngle(hip, knee, ankle)
        
        return if (angle > 95) FormError.SQUAT_DEPTH else null
    }
    
    private fun calculateAngle(
        firstPoint: android.graphics.PointF,
        midPoint: android.graphics.PointF,
        lastPoint: android.graphics.PointF
    ): Double {
        var result = Math.toDegrees(
            kotlin.math.atan2((lastPoint.y - midPoint.y).toDouble(), 
                (lastPoint.x - midPoint.x).toDouble()) -
            kotlin.math.atan2((firstPoint.y - midPoint.y).toDouble(), 
                (firstPoint.x - midPoint.x).toDouble())
        )
        result = abs(result)
        if (result > 180) result = 360 - result
        return result
    }
    
    enum class ExerciseType {
        SQUAT,
        DEADLIFT,
        BENCH_PRESS
    }
}
