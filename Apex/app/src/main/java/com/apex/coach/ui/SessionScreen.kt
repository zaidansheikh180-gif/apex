package com.apex.coach.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apex.coach.R
import com.apex.coach.ml.pose.RepCounter
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    workoutId: String,
    onSessionComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val vibrator = context.getSystemService(Vibrator::class.java)
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onCameraPermissionResult(isGranted)
    }
    
    // Check camera permission on launch
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onCameraPermissionResult(true)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    // Haptic feedback on rep
    LaunchedEffect(state.repCount) {
        if (state.lastRepWasCounted) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(50, 200)
            )
        }
    }
    
    // Out of frame detection for rest timer
    LaunchedEffect(state.isBodyInFrame) {
        if (!state.isBodyInFrame) {
            delay(1500) // 1.5 second threshold per PRD
            if (!state.isBodyInFrame && !state.isResting) {
                viewModel.startRestTimer()
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview (only if permission granted)
        if (state.hasCameraPermission) {
            CameraPreview(
                onPoseDetected = viewModel::onPoseDetected,
                lifecycleOwner = lifecycleOwner
            )
            
            // Skeleton Overlay
            state.currentPose?.let { pose ->
                SkeletonOverlay(
                    pose = pose,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Manual mode fallback
            ManualModePlaceholder(
                onManualRep = viewModel::manualRepIncrement
            )
        }
        
        // Top Bar
        TopAppBar(
            title = { Text(state.exerciseName) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Manual override always available
                IconButton(onClick = viewModel::manualRepIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Manual Count")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.5f),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Rep Counter Overlay
        RepCounterOverlay(
            currentRep = state.repCount,
            targetReps = state.targetReps,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
        
        // Form Error Toast
        state.formError?.let {
            FormErrorToast(
                message = when (it) {
                    RepCounter.FormError.SQUAT_DEPTH -> stringResource(R.string.form_toast_depth)
                    RepCounter.FormError.KNEE_VALGUS -> stringResource(R.string.form_toast_align)
                },
                onDismiss = viewModel::dismissFormError,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Rest Timer Overlay
        if (state.isResting) {
            RestTimerOverlay(
                secondsRemaining = state.restSecondsRemaining,
                onResume = viewModel::resumeFromRest,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Poor Lighting Warning
        if (state.poorLightingDetected) {
            PoorLightingBanner(
                onDismiss = viewModel::dismissLightingWarning,
                onSwitchToManual = {
                    viewModel.switchToManualMode()
                }
            )
        }
        
        // Session Complete Overlay
        if (state.isSessionComplete) {
            SessionCompleteDialog(
                totalTonnage = state.totalTonnage,
                formScore = state.formScore,
                completionTime = state.completionTimeMinutes,
                onFinish = onSessionComplete
            )
        }
        
        // Pro Paywall (after 3 free sessions)
        if (state.showPaywall) {
            ProPaywallDialog(
                onSubscribe = viewModel::initiatePurchase,
                onDismiss = onNavigateBack
            )
        }
        
        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Set Progress
            LinearProgressIndicator(
                progress = state.setProgress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.set_progress, state.currentSet, state.totalSets),
                    color = Color.White
                )
                
                Text(
                    text = stringResource(R.string.tonnage_display, state.sessionTonnage.toInt()),
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::skipExercise,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.skip_button))
                }
                
                Button(
                    onClick = viewModel::completeSet,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (state.isLastSet) stringResource(R.string.finish_workout)
                        else stringResource(R.string.complete_set)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onPoseDetected: (Pose) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                // Pose detection handled by PoseDetectorManager
                                imageProxy.close()
                            }
                        }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun SkeletonOverlay(
    pose: Pose,
    modifier: Modifier = Modifier
) {
    // Custom Canvas drawing of 33-point ML Kit skeleton
    Canvas(modifier = modifier) {
        val landmarks = pose.allPoseLandmarkss

        if (landmarks.isEmpty()) return@Canvas
        
        // Draw connections between landmarks
        val connections = listOf(
            // Torso
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
            // Arms
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            // Legs
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        )
        
        connections.forEach { (start, end) ->
            val startLandmark = pose.getPoseLandmark(start)
            val endLandmark = pose.getPoseLandmark(end)
            
            if (startLandmark != null && endLandmark != null) {
                drawLine(
                    color = Color.Green,
                    start = androidx.compose.ui.geometry.Offset(
                        startLandmark.position.x,
                        startLandmark.position.y
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        endLandmark.position.x,
                        endLandmark.position.y
                    ),
                    strokeWidth = 4f
                )
            }
        }
        
        // Draw landmarks
        landmarks.forEach { landmark ->
            drawCircle(
                color = Color.Yellow,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(
                    landmark.position.x,
                    landmark.position.y
                )
            )
        }
    }
}

@Composable
fun RepCounterOverlay(
    currentRep: Int,
    targetReps: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentRep",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "/ $targetReps",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Text(
                text = stringResource(R.string.reps_label),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun FormErrorToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(3000) // Auto-dismiss after 3 seconds
        onDismiss()
    }
    
    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RestTimerOverlay(
    secondsRemaining: Int,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.9f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.rest_timer_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.resume_button))
            }
        }
    }
}

@Composable
fun ManualModePlaceholder(
    onManualRep: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.manual_mode_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Text(
                text = stringResource(R.string.manual_mode_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onManualRep,
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.tap_to_count),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PoorLightingBanner(
    onDismiss: () -> Unit,
    onSwitchToManual: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.poor_lighting_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.poor_lighting_body),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss_button))
                }
                
                Button(onClick = onSwitchToManual) {
                    Text(stringResource(R.string.switch_manual_button))
                }
            }
        }
    }
}

@Composable
fun SessionCompleteDialog(
    totalTonnage: Double,
    formScore: Double?,
    completionTime: Int,
    onFinish: () -> Unit
) {
    val message = when {
        formScore == null -> stringResource(R.string.session_complete_mid)
        formScore > 85 -> stringResource(R.string.session_complete_high)
        else -> stringResource(R.string.session_complete_mid)
    }
    
    AlertDialog(
        onDismissRequest = onFinish,
        title = {
            Text(
                text = stringResource(R.string.session_complete_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Stats
                SessionStatRow(
                    label = stringResource(R.string.tonnage_stat),
                    value = "${totalTonnage.toInt()} kg"
                )
                
                SessionStatRow(
                    label = stringResource(R.string.time_stat),
                    value = "$completionTime min"
                )
                
                formScore?.let {
                    SessionStatRow(
                        label = stringResource(R.string.form_score_stat),
                        value = "${it.toInt()}/100"
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onFinish) {
                Text(stringResource(R.string.finish_button))
            }
        }
    )
}

@Composable
fun SessionStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProPaywallDialog(
    onSubscribe: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.paywall_title))
        },
        text = {
            Column {
                Text(stringResource(R.string.paywall_body))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Feature list
                listOf(
                    R.string.paywall_feature_1,
                    R.string.paywall_feature_2,
                    R.string.paywall_feature_3
                ).forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(feature))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubscribe) {
                Text(stringResource(R.string.subscribe_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.maybe_later_button))
            }
        }
    )
}
