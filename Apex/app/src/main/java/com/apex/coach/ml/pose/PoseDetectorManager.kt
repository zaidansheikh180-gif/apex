package com.apex.coach.ml.pose

import android.content.Context
import android.media.Image
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit Pose Detector Manager
 * Uses Accurate Pose Detection (33 landmarks)
 * Processing capped at 15 FPS per PRD Section 9 Constraints
 * All processing on-device, no cloud transmission (Privacy Constraint)
 */
@Singleton
class PoseDetectorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _currentPose = MutableStateFlow<Pose?>(null)
    val currentPose: Flow<Pose?> = _currentPose.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: Flow<Boolean> = _isProcessing.asStateFlow()
    
    // Accurate pose detector with 33 landmarks
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()
    
    private val detector: PoseDetector = PoseDetection.getClient(options)
    
    private var lastProcessTime = 0L
    private val minProcessIntervalMs = 66L // Cap at ~15 FPS (1000/15 = 66.6ms)
    
    fun processImage(imageProxy: ImageProxy, onPoseDetected: (Pose) -> Unit) {
        val currentTime = System.currentTimeMillis()
        
        // FPS cap enforcement
        if (currentTime - lastProcessTime < minProcessIntervalMs) {
            imageProxy.close()
            return
        }
        
        val mediaImage: Image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        
        _isProcessing.value = true
        
        detector.process(image)
            .addOnSuccessListener { pose ->
                lastProcessTime = currentTime
                _currentPose.value = pose
                onPoseDetected(pose)
                _isProcessing.value = false
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                _isProcessing.value = false
                imageProxy.close()
            }
    }
    
    fun close() {
        detector.close()
    }
}
