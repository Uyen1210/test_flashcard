package com.example.test_flashcard

import android.app.Application
import com.example.test_flashcard.data.AppDatabase
import com.example.test_flashcard.data.FlashcardRepository

class MyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy {
        FlashcardRepository(
            database.flashcardDao(),
            database.deckDao(),
            database.studyLogDao()
        )
    }
}