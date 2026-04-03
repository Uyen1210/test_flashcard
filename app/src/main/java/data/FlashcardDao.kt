package com.example.test_flashcard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    // Lấy tất cả các thẻ CẦN ÔN TẬP hôm nay (nextReviewDate <= thời gian hiện tại)
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentTime")
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>
    // Lấy tất cả bộ bài hiển thị ra màn hình
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>

    // Tạo bộ bài mới
    @Insert
    suspend fun insertDeck(deck: Deck)
}