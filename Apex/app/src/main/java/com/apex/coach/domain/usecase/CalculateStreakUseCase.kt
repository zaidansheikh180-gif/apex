package com.apex.coach.domain.usecase

import com.apex.coach.data.local.entity.StreakHistoryEntity
import com.apex.coach.data.local.entity.StreakStatus
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class StreakInfo(
    val currentStreak: Int,
    val last14Days: List<DayStatus>,
    val isFrozen: Boolean
)

data class DayStatus(
    val date: LocalDate,
    val status: StreakStatus,
    val isToday: Boolean = false
)

@Singleton
class CalculateStreakUseCase @Inject constructor() {
    
    fun getStreakInfo(): StreakInfo {
        val today = LocalDate.now()
        val last14Days = (0..13).map { daysAgo ->
            DayStatus(
                date = today.minusDays(daysAgo.toLong()),
                status = when {
                    daysAgo == 0 -> StreakStatus.COMPLETED
                    daysAgo < 3 -> StreakStatus.COMPLETED
                    daysAgo == 3 -> StreakStatus.MISSED_FROZEN
                    else -> StreakStatus.MISSED_BROKEN
                },
                isToday = daysAgo == 0
            )
        }.reversed()
        
        var streak = 0
        for (day in last14Days.reversed()) {
            if (day.status == StreakStatus.COMPLETED) {
                streak++
            } else if (day.status != StreakStatus.MISSED_FROZEN) {
                break
            }
        }
        
        return StreakInfo(
            currentStreak = streak,
            last14Days = last14Days,
            isFrozen = last14Days.any { it.status == StreakStatus.MISSED_FROZEN }
        )
    }
    
    fun calculateStreak(entities: List<StreakHistoryEntity>): Int {
        if (entities.isEmpty()) return 0
        
        val sortedEntities = entities.sortedByDescending { it.date }
        var streak = 0
        var currentDate = LocalDate.now()
        
        for (entity in sortedEntities) {
            if (entity.status == StreakStatus.COMPLETED) {
                if (entity.date == currentDate || entity.date == currentDate.minusDays(1)) {
                    streak++
                    currentDate = entity.date.minusDays(1)
                } else {
                    break
                }
            } else if (entity.status == StreakStatus.MISSED_FROZEN) {
                currentDate = entity.date.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }
}
