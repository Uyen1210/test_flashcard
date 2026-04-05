package com.example.test_flashcard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>

    @Insert
    suspend fun insertDeck(deck: Deck)
}