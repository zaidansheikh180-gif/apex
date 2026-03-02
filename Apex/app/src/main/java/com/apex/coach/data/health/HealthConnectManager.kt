package com.apex.coach.data.health

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    fun openInstallIntent() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("market://details?id=com.google.android.apps.healthdata")
        }
        context.startActivity(intent)
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<HealthPermission>, Set<HealthPermission>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun readLatestBiometrics(): BiometricData? {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(1, ChronoUnit.DAYS)

            // Read HRV
            val hrvRequest = healthConnectClient.readRecords(
                HeartRateVariabilityRmssdRecord::class,
                TimeRangeFilter.between(startTime, endTime)
            )
            val latestHrv = hrvRequest.records.maxByOrNull { it.time }?.rmssd

            // Read Sleep
            val sleepRequest = healthConnectClient.readRecords(
                SleepSessionRecord::class,
                TimeRangeFilter.between(startTime, endTime)
            )
            val sleepDuration = sleepRequest.records.sumOf { 
                ChronoUnit.MINUTES.between(it.startTime, it.endTime) 
            } / 60.0

            // Read Resting Heart Rate (use lowest heart rate as proxy)
            val hrRequest = healthConnectClient.readRecords(
                HeartRateRecord::class,
                TimeRangeFilter.between(startTime, endTime)
            )
            val restingHr = hrRequest.records.flatMap { it.samples }
                .minByOrNull { it.beatsPerMinute }?.beatsPerMinute?.toDouble()

            if (latestHrv != null && restingHr != null) {
                BiometricData(
                    hrv = latestHrv,
                    sleepHours = sleepDuration,
                    rhr = restingHr,
                    timestamp = java.time.LocalDateTime.now()
                )
            } else null

        } catch (e: Exception) {
            // Log the exception here in a real app
            e.printStackTrace()
            null
        }
    }
}

data class BiometricData(
    val hrv: Double,
    val sleepHours: Double,
    val rhr: Double,
    val timestamp: java.time.LocalDateTime
)
