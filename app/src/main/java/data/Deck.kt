package com.example.test_flashcard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,               // Tên bộ bài (VD: Earth Science)
    val category: String,           // Danh mục (VD: Science & Environment)
    val description: String = ""    // Mô tả thêm
)

data class DeckWithProgress(
    val deck: Deck,
    val totalCards: Int,
    val learnedCards: Int
) {
    val progress: Float
        get() = if (totalCards > 0) learnedCards.toFloat() / totalCards else 0f

    val progressText: String
        get() = "${(progress * 100).toInt()}%"
}