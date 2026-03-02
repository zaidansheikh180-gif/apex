package com.apex.coach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apex.coach.data.local.entity.StreakHistoryEntity
import com.apex.coach.data.local.entity.StreakStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StreakDao {
    
    @Query("SELECT * FROM streak_history WHERE date >= :startDate ORDER BY date DESC")
    fun getStreakHistoryFrom(startDate: LocalDate): Flow<List<StreakHistoryEntity>>
    
    @Query("SELECT * FROM streak_history WHERE date = :date")
    suspend fun getStreakForDate(date: LocalDate): StreakHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakHistoryEntity)
    
    @Query("""
        SELECT COUNT(*) FROM streak_history 
        WHERE status = :completedStatus 
        AND date >= :startDate
    """)
    suspend fun countCompletedDaysSince(
        completedStatus: StreakStatus = StreakStatus.COMPLETED,
        startDate: LocalDate
    ): Int
    
    @Query("""
        SELECT * FROM streak_history 
        WHERE date > :date 
        ORDER BY date ASC 
        LIMIT 1
    """)
    suspend fun getNextDayAfter(date: LocalDate): StreakHistoryEntity?
    
    @Query("""
        SELECT COUNT(*) FROM streak_history 
        WHERE status = :frozenStatus 
        AND date >= :windowStart
    """)
    suspend fun countFreezesInWindow(
        frozenStatus: StreakStatus = StreakStatus.MISSED_FROZEN,
        windowStart: LocalDate
    ): Int
}
