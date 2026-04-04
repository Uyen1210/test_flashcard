package com.example.test_flashcard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface FlashcardDao {
    // 1. Thêm thẻ mới (Dùng cho tính năng Add Card và Import CSV)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard)

    // 2. Cập nhật thẻ (Dùng cho thuật toán SRS khi bấm Dễ/Khó)
    @Update
    suspend fun updateCard(card: Flashcard)

    // 3. Lấy danh sách thẻ theo bộ bài
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Int): Flow<List<Flashcard>>

    // 4. Các hàm tính toán tiến độ (Đã làm ở bước trước)
    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getTotalCount(deckId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND repetitions > 0")
    fun getLearnedCount(deckId: Int): Flow<Int>

    // 5. Reset tiến độ
    @Query("UPDATE flashcards SET repetitions = 0, interval = 1, easeFactor = 2.5, nextReviewDate = 0 WHERE deckId = :deckId")
    suspend fun resetDeckProgress(deckId: Int)
}