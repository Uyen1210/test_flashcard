package com.example.test_flashcard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Khai báo các bảng (entities) có trong DB
@Database(entities = [Deck::class, Flashcard::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Cấp phát Database theo chuẩn Singleton (Chỉ tạo 1 lần duy nhất để tránh rò rỉ bộ nhớ)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "the_scoring_secret_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}