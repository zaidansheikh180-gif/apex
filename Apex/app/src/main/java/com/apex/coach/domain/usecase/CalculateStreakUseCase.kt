package com.apex.coach.domain.usecase

import com.apex.coach.data.local.entity.StreakHistoryEntity
import com.apex.coach.data.local.entity.StreakStatus
import kotlinx.datetime.*
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
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val last14Days = (0..13).map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            DayStatus(
                date = date,
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
        var currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        for (entity in sortedEntities) {
            // Note: StreakHistoryEntity might still use java.time.LocalDate if it's an Android-only entity.
            // Ideally, we'd migrate those too. For now, we assume comparison works or use .toKotlinLocalDate()
            if (entity.status == StreakStatus.COMPLETED) {
                if (entity.date == currentDate || entity.date == currentDate.minus(1, DateTimeUnit.DAY)) {
                    streak++
                    currentDate = entity.date.minus(1, DateTimeUnit.DAY)
                } else {
                    break
                }
            } else if (entity.status == StreakStatus.MISSED_FROZEN) {
                currentDate = entity.date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        
        return streak
    }
}
