package com.example.test_flashcard.ui.theme.practice

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_flashcard.data.*
import com.example.test_flashcard.domain.SpacedRepetitionAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModel(private val repository: FlashcardRepository) : ViewModel() {

    val decksWithProgress: Flow<List<DeckWithProgress>> = repository.getAllDecks().flatMapLatest { decks ->
        if (decks.isEmpty()) {
            flowOf(emptyList())
        } else {
            val flowList = decks.map { deck ->
                combine(
                    repository.getTotalCount(deck.id),
                    repository.getLearnedCount(deck.id)
                ) { total, learned ->
                    DeckWithProgress(deck, total, learned)
                }
            }
            combine(flowList) { it.toList() }
        }
    }

    val streakCount: StateFlow<Int> = repository.getUniqueStudyDays()
        .map { calculateStreak(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalLearnedCount: StateFlow<Int> = repository.getTotalLearnedAllTime()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private val _currentCard = MutableStateFlow<Flashcard?>(null)
    val currentCard: StateFlow<Flashcard?> = _currentCard

    private var reviewQueue = mutableListOf<Flashcard>()

    fun getCardsByDeck(deckId: Int): Flow<List<Flashcard>> = repository.getCardsByDeck(deckId)

    fun updateCardContent(card: Flashcard, newFront: String, newBack: String) {
        viewModelScope.launch {
            repository.updateCard(card.copy(frontText = newFront, backText = newBack))
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    // Hàm xóa bộ bài
    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    fun startPractice(deckId: Int) {
        viewModelScope.launch {
            val cards = repository.getCardsByDeck(deckId).first()
            reviewQueue = cards.toMutableList()
            _currentCard.value = reviewQueue.removeFirstOrNull()
        }
    }

    fun onUserAnswered(quality: Int) {
        val card = _currentCard.value ?: return
        viewModelScope.launch {
            val updatedCard = SpacedRepetitionAlgorithm.calculateNextReview(card, quality)
            repository.updateCard(updatedCard)
            if (quality == 0) {
                reviewQueue.add(updatedCard)
            }
            _currentCard.value = reviewQueue.removeFirstOrNull()
        }
    }

    fun addNewCard(front: String, back: String, deckId: Int) {
        viewModelScope.launch {
            repository.insertCard(Flashcard(deckId = deckId, frontText = front, backText = back))
        }
    }

    fun addNewDeck(name: String, category: String) {
        viewModelScope.launch {
            repository.insertDeck(Deck(name = name, category = category))
        }
    }

    fun resetAllCardsInDeck(deckId: Int) {
        viewModelScope.launch {
            repository.resetDeckProgress(deckId)
        }
    }

    fun importCsv(uri: Uri, contentResolver: ContentResolver, deckId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = inputStream?.bufferedReader()
                    reader?.lineSequence()?.forEach { line ->
                        val parts = line.split(",")
                        if (parts.size >= 2) {
                            repository.insertCard(Flashcard(deckId = deckId, frontText = parts[0].trim(), backText = parts[1].trim()))
                        }
                    }
                    inputStream?.close()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun saveStudyLog(deckId: Int, count: Int) {
        viewModelScope.launch {
            repository.insertLog(StudyLog(deckId = deckId, cardsLearned = count))
        }
    }

    private fun calculateStreak(studyDays: List<Long>): Int {
        if (studyDays.isEmpty()) return 0
        val today = System.currentTimeMillis() / 86400000
        var streak = 0
        var currentDay = today
        if (studyDays.first() != today && studyDays.first() != today - 1) return 0
        if (studyDays.first() == today - 1) currentDay = today - 1
        for (day in studyDays) {
            if (day == currentDay) {
                streak++
                currentDay--
            } else if (day < currentDay) break
        }
        return streak
    }
}