package com.example.test_flashcard.ui.theme.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test_flashcard.data.FlashcardRepository

class PracticeViewModelFactory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PracticeViewModel(repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy class ViewModel này!")
    }
}