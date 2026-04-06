package com.example.test_flashcard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>

    @Insert
    suspend fun insertDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    // Xóa tất cả thẻ bài thuộc về bộ bài này khi xóa bộ bài
    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteCardsByDeckId(deckId: Int)
}