package com.example.test_flashcard.data

import kotlinx.coroutines.flow.Flow

class FlashcardRepository(private val flashcardDao: FlashcardDao) {
    fun getCardsToReview(deckId: Int): Flow<List<Flashcard>> {
        val currentTime = System.currentTimeMillis()
        return flashcardDao.getCardsToReview(deckId, currentTime)
    }

    suspend fun updateCard(card: Flashcard) {
        flashcardDao.updateFlashcard(card)
    }

    // THÊM HÀM NÀY ĐỂ TẠO THẺ MỚI
    suspend fun insertCard(card: Flashcard) {
        flashcardDao.insertFlashcard(card)
    }
    fun getAllDecks(): Flow<List<Deck>> {
        return flashcardDao.getAllDecks()
    }

    suspend fun insertDeck(deck: Deck) {
        flashcardDao.insertDeck(deck)
    }

    suspend fun resetDeckProgress(deckId: Int) {
        flashcardDao.resetDeckProgress(deckId)
    }

}