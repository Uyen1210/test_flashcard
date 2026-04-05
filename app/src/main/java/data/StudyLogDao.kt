package com.example.test_flashcard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyLogDao {
    @Insert
    suspend fun insertLog(log: StudyLog)

    @Query("SELECT * FROM study_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<StudyLog>>

    @Query("SELECT DISTINCT (date / 86400000) FROM study_logs ORDER BY date DESC")
    fun getUniqueStudyDays(): Flow<List<Long>>
}