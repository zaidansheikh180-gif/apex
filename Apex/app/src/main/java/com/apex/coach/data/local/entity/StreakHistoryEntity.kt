package com.apex.coach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "streak_history")
data class StreakHistoryEntity(
    @PrimaryKey
    val date: LocalDate,
    val status: StreakStatus,
    val sessionId: String? = null,
    val isFrozen: Boolean = false
)

enum class StreakStatus {
    COMPLETED,
    MISSED_FROZEN,
    MISSED_BROKEN,
    REST_DAY,
    PLANNED_REST
}
