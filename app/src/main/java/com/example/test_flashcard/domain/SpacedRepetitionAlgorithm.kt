package com.example.test_flashcard.domain

import com.example.test_flashcard.data.Flashcard
import java.util.concurrent.TimeUnit

object SpacedRepetitionAlgorithm {

    // quality: 0 (Quên sạch), 1 (Hơi khó), 2 (Bình thường), 3 (Quá dễ)
    fun calculateNextReview(card: Flashcard, quality: Int): Flashcard {
        var newInterval = card.interval
        var newRepetitions = card.repetitions
        var newEaseFactor = card.easeFactor

        if (quality >= 2) { // Trả lời đúng (Bình thường hoặc Dễ)
            if (newRepetitions == 0) {
                newInterval = 1
            } else if (newRepetitions == 1) {
                newInterval = 6
            } else {
                newInterval = Math.round(newInterval * newEaseFactor)
            }
            newRepetitions++
        } else { // Trả lời sai (Quên sạch hoặc Hơi khó)
            newRepetitions = 0
            newInterval = 1
        }

        // Cập nhật Ease Factor (Hệ số độ khó)
        newEaseFactor += (0.1f - (3 - quality) * (0.08f + (3 - quality) * 0.02f))
        if (newEaseFactor < 1.3f) newEaseFactor = 1.3f // Mức tối thiểu

        // Tính toán ngày ôn tiếp theo
        val msInDay = TimeUnit.DAYS.toMillis(1)
        val newNextReviewDate = System.currentTimeMillis() + (newInterval * msInDay)

        return card.copy(
            interval = newInterval,
            repetitions = newRepetitions,
            easeFactor = newEaseFactor,
            nextReviewDate = newNextReviewDate
        )
    }
}