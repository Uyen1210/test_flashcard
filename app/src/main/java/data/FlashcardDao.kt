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

    // Lấy tất cả các thẻ CẦN ÔN TẬP hôm nay
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentTime")
    fun getCardsToReview(deckId: Int, currentTime: Long): Flow<List<Flashcard>>
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>


    @Insert
    suspend fun insertDeck(deck: Deck)
    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getTotalCards(deckId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND repetitions > 0")
    fun getLearnedCards(deckId: Int): Flow<Int>

    @Query("UPDATE flashcards SET repetitions = 0, interval = 1, easeFactor = 2.5, nextReviewDate = 0 WHERE deckId = :deckId")
    suspend fun resetDeckProgress(deckId: Int)

}