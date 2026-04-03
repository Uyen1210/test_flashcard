package com.example.test_flashcard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,                // ID của bộ bài chứa thẻ này
    val frontText: String,          // Mặt trước (Câu hỏi/Từ vựng)
    val backText: String,           // Mặt sau (Đáp án/Định nghĩa)

    // Các thông số cho thuật toán Spaced Repetition (SRS)
    var easeFactor: Float = 2.5f,   // Hệ số độ khó (Mặc định 2.5)
    var interval: Int = 0,          // Số ngày tới lần ôn tiếp theo
    var repetitions: Int = 0,       // Số lần đã trả lời đúng liên tiếp
    var nextReviewDate: Long = System.currentTimeMillis() // Thời điểm cần ôn lại (Timestamp)
)