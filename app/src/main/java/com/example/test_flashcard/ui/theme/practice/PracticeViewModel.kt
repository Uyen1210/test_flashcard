package com.example.test_flashcard.ui.theme.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_flashcard.data.Deck
import com.example.test_flashcard.data.Flashcard
import com.example.test_flashcard.data.FlashcardRepository
import com.example.test_flashcard.domain.SpacedRepetitionAlgorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PracticeViewModel(private val repository: FlashcardRepository) : ViewModel() {

    val allDecks: StateFlow<List<Deck>> = repository.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCard = MutableStateFlow<Flashcard?>(null)
    val currentCard: StateFlow<Flashcard?> = _currentCard

    private var reviewQueue = mutableListOf<Flashcard>()

    init {
        createDummyDataIfNeeded()
    }

    private fun createDummyDataIfNeeded() {
        viewModelScope.launch {
            val existingDecks = repository.getAllDecks().first()
            if (existingDecks.isEmpty()) {
                repository.insertDeck(Deck(name = "Earth Science", category = "Science & Environment"))
                repository.insertDeck(Deck(name = "Art", category = "Arts"))

                val newDecks = repository.getAllDecks().first()
                if (newDecks.isNotEmpty()) {
                    repository.insertCard(Flashcard(deckId = newDecks[0].id, frontText = "Core", backText = "Lõi Trái Đất"))
                }
            }
        }
    }


    fun startPractice(deckId: Int) {
        viewModelScope.launch {
            val cards = repository.getCardsToReview(deckId).first()
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
            val newCard = Flashcard(
                deckId = deckId,
                frontText = front,
                backText = back
            )
            repository.insertCard(newCard)
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
}