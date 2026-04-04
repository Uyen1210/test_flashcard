package com.example.test_flashcard

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.test_flashcard.ui.theme.Test_flashcardTheme
import com.example.test_flashcard.ui.theme.practice.DashboardScreen
import com.example.test_flashcard.ui.theme.practice.PracticeViewModel
import com.example.test_flashcard.ui.theme.practice.PracticeViewModelFactory
import com.example.test_flashcard.worker.ReminderWorker
import com.example.test_flashcrad.ui.theme.practice.PracticeScreen
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        // 2. Thiết lập thông báo nhắc nhở hàng ngày
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        setContent {
            Test_flashcardTheme {
                val application = LocalContext.current.applicationContext as MyApplication
                val practiceViewModel: PracticeViewModel = viewModel(
                    factory = PracticeViewModelFactory(application.repository)
                )

                // QUẢN LÝ TRẠNG THÁI
                var currentScreen by remember { mutableStateOf("dashboard") }
                var selectedDeckIdForImport by remember { mutableIntStateOf(-1) }
                var activeDeckId by remember { mutableIntStateOf(-1) }

                val currentCard by practiceViewModel.currentCard.collectAsState()
                val decksWithProgress by practiceViewModel.decksWithProgress.collectAsState(initial = emptyList())
                val streak by practiceViewModel.streakCount.collectAsState()

                // TRÌNH CHỌN FILE
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        if (selectedDeckIdForImport != -1) {
                            practiceViewModel.importCsv(it, contentResolver, selectedDeckIdForImport)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentScreen == "dashboard") {
                        DashboardScreen(
                            decks = decksWithProgress,
                            //streak = streak, // Hãy truyền biến này vào DashboardScreen để hiển thị 🔥
                            onDeckClick = { deckId ->
                                activeDeckId = deckId
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
                            },
                            onImportClick = { deckId: Int ->
                                selectedDeckIdForImport = deckId
                                filePickerLauncher.launch("text/*")
                            }
                        )
                    } else if (currentScreen == "practice") {
                        val card = currentCard
                        if (card == null) {
                            // Khi hết bài: Tự động lưu log
                            LaunchedEffect(Unit) {
                                if (activeDeckId != -1) {
                                    practiceViewModel.saveStudyLog(activeDeckId, 1)
                                }
                            }
                            ResultScreen(onBack = { currentScreen = "dashboard" })
                        } else {
                            PracticeScreen(
                                frontText = card.frontText,
                                backText = card.backText,
                                onAnswerSelected = { quality ->
                                    practiceViewModel.onUserAnswered(quality)
                                },
                                onSpeak = { speakOut(card.frontText) },
                                onExit = { currentScreen = "dashboard" }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// Đảm bảo dấu đóng ngoặc này là của class MainActivity { ... }
// }

@Composable
fun ResultScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chúc mừng!\nBạn đã hoàn thành bài học.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Quay về Dashboard")
        }
    }
}