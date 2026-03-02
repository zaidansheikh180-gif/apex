package com.apex.coach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.apex.coach.domain.model.WorkoutMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "workout_sessions"
)
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val programId: String,
    val dayNumber: Int,
    val scheduledDate: LocalDate,
    val completedDate: LocalDateTime? = null,
    val mode: WorkoutMode,
    val apexScoreAtStart: Int,
    val totalTonnage: Double = 0.0,
    val completionPercentage: Double = 0.0,
    val durationMinutes: Int = 0,
    val arUsed: Boolean = false,
    val formScore: Double? = null,
    val wasCompleted: Boolean = false
)
