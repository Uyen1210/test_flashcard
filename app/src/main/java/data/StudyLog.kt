package com.example.test_flashcard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val date: Long = System.currentTimeMillis(),
    val cardsLearned: Int
)