package com.example.test_flashcard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard)

    @Update
    suspend fun updateCard(card: Flashcard)

    @Delete
    suspend fun deleteCard(card: Flashcard)

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Int): Flow<List<Flashcard>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getTotalCount(deckId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND repetitions > 0")
    fun getLearnedCount(deckId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE repetitions > 0")
    fun getTotalLearnedAllTime(): Flow<Int>

    @Query("UPDATE flashcards SET repetitions = 0, interval = 1, easeFactor = 2.5, nextReviewDate = 0 WHERE deckId = :deckId")
    suspend fun resetDeckProgress(deckId: Int)
}