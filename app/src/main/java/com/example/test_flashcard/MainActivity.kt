package com.example.test_flashcard

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_flashcard.ui.theme.Test_flashcardTheme
import com.example.test_flashcard.ui.theme.practice.DashboardScreen
import com.example.test_flashcard.ui.theme.practice.PracticeViewModel
import com.example.test_flashcard.ui.theme.practice.PracticeViewModelFactory
import com.example.test_flashcrad.ui.theme.practice.PracticeScreen
import java.util.*

class MainActivity : ComponentActivity() {
    // 1. Khai báo TTS
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Khởi tạo TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        setContent {
            Test_flashcardTheme {
                val application = LocalContext.current.applicationContext as MyApplication
                val practiceViewModel: PracticeViewModel = viewModel(
                    factory = PracticeViewModelFactory(application.repository)
                )

                var currentScreen by remember { mutableStateOf("dashboard") }
                val currentCard by practiceViewModel.currentCard.collectAsState()
                val decks by practiceViewModel.allDecks.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentScreen == "dashboard") {
                        DashboardScreen(
                            decks = decks,
                            onDeckClick = { deckId ->
                                practiceViewModel.startPractice(deckId)
                                currentScreen = "practice"
                            },
                            onAddCard = { front, back, deckId ->
                                practiceViewModel.addNewCard(front, back, deckId)
                            },
                            onAddDeck = { name, cat ->
                                practiceViewModel.addNewDeck(name, cat)
                            },
                            onResetDeck = { deckId ->
                                practiceViewModel.resetAllCardsInDeck(deckId)
                            }
                        )
                    } else if (currentScreen == "practice") {
                        val card = currentCard
                        if (card == null) {
                            ResultScreen(onBack = { currentScreen = "dashboard" })
                        } else {
                            PracticeScreen(
                                frontText = card.frontText,
                                backText = card.backText,
                                onAnswerSelected = { quality : Int ->
                                    practiceViewModel.onUserAnswered(quality)
                                },
                                onSpeak = {
                                    speakOut(card.frontText)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. Hàm phát âm (Rất quan trọng - nãy bạn thiếu hàm này)
    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // 4. Giải phóng bộ nhớ
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// Màn hình kết thúc tách riêng cho gọn
@Composable
fun ResultScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chúc mừng!\nBạn đã học xong bộ bài này.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Quay về Trang chủ")
        }
    }
}