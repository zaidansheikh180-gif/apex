package com.apex.coach.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apex.coach.domain.model.WorkoutMode

// ============================================================================
// APEX DESIGN SYSTEM - Centralized Design & Visual Features
// ============================================================================
// This file centralizes all design tokens, colors, typography, spacing,
// shapes, animations, and component styles for the Apex Coach app.
// ============================================================================

// ----------------------------------------------------------------------------
// SECTION: COLORS
// ----------------------------------------------------------------------------

object ApexColors {
    // Brand Colors
    val ApexGreen = Color(0xFF4CAF50)
    val ApexYellow = Color(0xFFFFC107)
    val ApexBlue = Color(0xFF2196F3)
    val ApexRed = Color(0xFFF44336)
    
    // Dark Theme Surfaces
    val DarkBackground = Color(0xFF0A0A0A)
    val DarkSurface = Color(0xFF1C1C1C)
    val DarkSurfaceVariant = Color(0xFF2C2C2C)
    
    // Skeleton Overlay Colors
    val SkeletonLineColor = Color.Green
    val SkeletonPointColor = Color.Yellow
    
    // Overlay Backgrounds
    val OverlayDark = Color.Black.copy(alpha = 0.5f)
    val OverlayDarkMedium = Color.Black.copy(alpha = 0.7f)
    val OverlayDarkHeavy = Color.Black.copy(alpha = 0.9f)
    
    // Text on Dark
    val TextWhite = Color.White
    val TextWhiteSecondary = Color.White.copy(alpha = 0.7f)
    val TextWhiteTertiary = Color.White.copy(alpha = 0.5f)
    
    // Mode-Specific Colors
    fun getModeColor(mode: WorkoutMode): Color = when (mode) {
        WorkoutMode.FULL -> ApexGreen
        WorkoutMode.EXPRESS -> ApexYellow
        WorkoutMode.REHAB -> ApexBlue
    }
    
    fun getModeBackgroundColor(mode: WorkoutMode): Color = 
        getModeColor(mode).copy(alpha = 0.2f)
    
    // Streak Status Colors
    fun getStreakCompletedColor() = MaterialTheme.colorScheme.primary
    fun getStreakFrozenColor() = MaterialTheme.colorScheme.tertiary
    fun getStreakBrokenColor() = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    fun getStreakPendingColor() = MaterialTheme.colorScheme.surfaceVariant
}

// ----------------------------------------------------------------------------
// SECTION: TYPOGRAPHY
// ----------------------------------------------------------------------------

object ApexTypography {
    // Display Styles
    val DisplayLarge = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Bold
    )
    
    val DisplayMedium = MaterialTheme.typography.displayMedium.copy(
        fontWeight = FontWeight.Bold
    )
    
    // Headline Styles
    val HeadlineLarge = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Bold
    )
    
    val HeadlineMedium = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold
    )
    
    val HeadlineSmall = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold
    )
    
    // Title Styles
    val TitleLarge = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    )
    
    val TitleMedium = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    )
    
    // Body Styles
    val BodyLarge = MaterialTheme.typography.bodyLarge
    val BodyMedium = MaterialTheme.typography.bodyMedium
    val BodySmall = MaterialTheme.typography.bodySmall
    
    // Label Styles
    val LabelLarge = MaterialTheme.typography.labelLarge
    val LabelMedium = MaterialTheme.typography.labelMedium
}

// ----------------------------------------------------------------------------
// SECTION: DIMENSIONS & SPACING
// ----------------------------------------------------------------------------

object ApexDimensions {
    // Standard Padding
    val PaddingTiny = 4.dp
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    val PaddingXLarge = 32.dp
    
    // Standard Margins
    val MarginSmall = 8.dp
    val MarginMedium = 12.dp
    val MarginLarge = 16.dp
    val MarginXLarge = 20.dp
    
    // Component Sizes
    val GaugeSizeSmall = 120.dp
    val GaugeSizeMedium = 150.dp
    val GaugeSizeLarge = 200.dp
    
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 48.dp
    val IconSizeXLarge = 64.dp
    
    val StreakDotSize = 20.dp
    val ButtonSizeLarge = 120.dp
    
    // Card Sizes
    val CardCornerRadiusSmall = 8.dp
    val CardCornerRadiusMedium = 12.dp
    val CardCornerRadiusLarge = 16.dp
    val CardCornerRadiusXLarge = 24.dp
    
    // Stroke Widths
    val GaugeStrokeWidth = 20.dp
    val SkeletonStrokeWidth = 4f
    val SkeletonPointRadius = 6f
}

// ----------------------------------------------------------------------------
// SECTION: SHAPES
// ----------------------------------------------------------------------------

object ApexShapes {
    val Tiny = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val XLarge = RoundedCornerShape(24.dp)
    
    val Circle = CircleShape
    
    // Common shape modifiers
    val CardShape = RoundedCornerShape(ApexDimensions.CardCornerRadiusLarge)
    val ChipShape = RoundedCornerShape(ApexDimensions.CardCornerRadiusMedium)
    val ButtonShape = RoundedCornerShape(ApexDimensions.CardCornerRadiusSmall)
    val OverlayShape = RoundedCornerShape(16.dp)
    val OverlayShapeLarge = RoundedCornerShape(24.dp)
}

// ----------------------------------------------------------------------------
// SECTION: ANIMATIONS
// ----------------------------------------------------------------------------

object ApexAnimations {
    // Duration Constants
    val DurationFast = 150
    val DurationMedium = 300
    val DurationSlow = 500
    val DurationVerySlow = 1000
    val DurationGauge = 1500
    
    // Delay Constants
    val DelayRestTrigger = 1500L  // 1.5 second threshold for rest timer
    val DelayToastDismiss = 3000L // 3 seconds for form error toast
    val DelayCountdown = 1000L
    
    // Haptic Feedback
    val HapticRepDuration = 50L
    val HapticRepAmplitude = 200
    
    // Animation Specs
    val GaugeAnimationSpec = tween<Float>(
        durationMillis = DurationGauge,
        easing = FastOutSlowInEasing
    )
    
    val FadeInAnimationSpec = tween<Float>(
        durationMillis = DurationMedium,
        easing = FastOutSlowInEasing
    )
}

// ----------------------------------------------------------------------------
// SECTION: COMPONENT STYLES
// ----------------------------------------------------------------------------

object ApexComponentStyles {
    // Card Styles
    object Card {
        val SurfaceVariantColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        val SurfaceColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
        
        val PrimaryContainerColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
        
        val ErrorContainerColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    }
    
    // TopAppBar Styles
    object TopAppBar {
        val OverlayColors = TopAppBarDefaults.topAppBarColors(
            containerColor = ApexColors.OverlayDark,
            titleContentColor = ApexColors.TextWhite,
            navigationIconContentColor = ApexColors.TextWhite,
            actionIconContentColor = ApexColors.TextWhite
        )
    }
}

// ----------------------------------------------------------------------------
// SECTION: SESSION OVERLAY COMPONENTS
// Reusable animated components for workout sessions
// ----------------------------------------------------------------------------

/**
 * Animated circular gauge component for displaying progress values
 * Used in Apex Score display and other progress indicators
 */
@Composable
fun ApexAnimatedGauge(
    value: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surface,
    strokeWidth: androidx.compose.ui.unit.Dp = ApexDimensions.GaugeStrokeWidth
) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = value,
            animationSpec = ApexAnimations.GaugeAnimationSpec
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background arc
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            color = trackColor,
            strokeWidth = strokeWidth,
            trackColor = trackColor
        )
        
        // Value arc
        CircularProgressIndicator(
            progress = animatedValue.value / maxValue,
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Pre-configured animated gauge for Apex Score display
 * Score is displayed as 0-100 with animated fill
 */
@Composable
fun ApexScoreGaugeAnimated(
    score: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: androidx.compose.ui.unit.Dp = ApexDimensions.GaugeSizeLarge
) {
    ApexAnimatedGauge(
        value = score.toFloat(),
        maxValue = 100f,
        modifier = modifier.size(size),
        color = color,
        strokeWidth = ApexDimensions.GaugeStrokeWidth
    )
}

/**
 * Individual streak dot for 14-day streak visualization
 * Shows completion status, frozen state, and current day indicator
 */
@Composable
fun ApexStreakDot(
    isCompleted: Boolean,
    isFrozen: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCompleted -> ApexColors.getStreakCompletedColor()
        isFrozen -> ApexColors.getStreakFrozenColor()
        else -> ApexColors.getStreakPendingColor()
    }
    
    Box(
        modifier = modifier
            .size(ApexDimensions.StreakDotSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday) Modifier.padding(2.dp)
                else Modifier
            )
    )
}

/**
 * Counter overlay surface for rep counting displays
 * Used in session screen for rep counter overlay
 */
@Composable
fun ApexCounterOverlay(
    modifier: Modifier = Modifier,
    content: @Composable Column.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = ApexColors.OverlayDarkMedium,
        shape = ApexShapes.OverlayShape
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/**
 * Rest timer overlay surface for rest periods between sets
 * Darker background for emphasis during rest
 */
@Composable
fun ApexRestTimerOverlay(
    modifier: Modifier = Modifier,
    content: @Composable Column.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = ApexColors.OverlayDarkHeavy,
        shape = ApexShapes.OverlayShapeLarge
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
