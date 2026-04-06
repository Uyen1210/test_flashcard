package com.example.test_flashcard.data

import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val flashcardDao: FlashcardDao,
    private val deckDao: DeckDao,
    private val studyLogDao: StudyLogDao
) {
    fun getAllDecks() = deckDao.getAllDecks()

    suspend fun insertCard(card: Flashcard) = flashcardDao.insertCard(card)
    suspend fun updateCard(card: Flashcard) = flashcardDao.updateCard(card)
    suspend fun deleteCard(card: Flashcard) = flashcardDao.deleteCard(card)

    fun getTotalCount(deckId: Int): Flow<Int> = flashcardDao.getTotalCount(deckId)
    fun getLearnedCount(deckId: Int): Flow<Int> = flashcardDao.getLearnedCount(deckId)

    fun getTotalLearnedAllTime(): Flow<Int> = flashcardDao.getTotalLearnedAllTime()

    suspend fun resetDeckProgress(deckId: Int) = flashcardDao.resetDeckProgress(deckId)

    suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)
    suspend fun deleteDeck(deck: Deck) {
        // Xóa tất cả thẻ bài thuộc về bộ bài này trước
        deckDao.deleteCardsByDeckId(deck.id)
        // Sau đó mới xóa bộ bài
        deckDao.deleteDeck(deck)
    }

    suspend fun insertLog(log: StudyLog) = studyLogDao.insertLog(log)
    fun getAllLogs(): Flow<List<StudyLog>> = studyLogDao.getAllLogs()
    fun getUniqueStudyDays(): Flow<List<Long>> = studyLogDao.getUniqueStudyDays()
    fun getCardsByDeck(deckId: Int): Flow<List<Flashcard>> = flashcardDao.getCardsByDeck(deckId)
}