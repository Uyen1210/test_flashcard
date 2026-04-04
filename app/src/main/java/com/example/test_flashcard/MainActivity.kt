package com.example.test_flashcard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
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
import androidx.core.app.NotificationCompat
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

        setupDailyReminder()

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
                var selectedDeckIdForImport by remember { mutableIntStateOf(-1) }
                var activeDeckId by remember { mutableIntStateOf(-1) }

                val currentCard by practiceViewModel.currentCard.collectAsState()
                val decksWithProgress by practiceViewModel.decksWithProgress.collectAsState(initial = emptyList())
                val streak by practiceViewModel.streakCount.collectAsState()

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        if (selectedDeckIdForImport != -1) {
                            practiceViewModel.importCsv(it, contentResolver, selectedDeckIdForImport)
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (currentScreen == "dashboard") {
                        DashboardScreen(
                            decks = decksWithProgress,
                            onDeckClick = { deckId ->
                                activeDeckId = deckId
                                practiceViewModel.startPractice(deckId)
                                currentScreen = "practice"
                            },
                            onAddCard = { f, b, id -> practiceViewModel.addNewCard(f, b, id) },
                            onAddDeck = { n, c -> practiceViewModel.addNewDeck(n, c) },
                            onResetDeck = { id -> practiceViewModel.resetAllCardsInDeck(id) },
                            onImportClick = { id ->
                                selectedDeckIdForImport = id
                                filePickerLauncher.launch("text/*")
                            }
                        )
                    } else if (currentScreen == "practice") {
                        val card = currentCard
                        if (card == null) {
                            LaunchedEffect(Unit) {
                                if (activeDeckId != -1) {
                                    practiceViewModel.saveStudyLog(activeDeckId, 1)
                                    sendCongratulationNotification("Bộ bài đã hoàn thành")
                                }
                            }
                            ResultScreen(onBack = { currentScreen = "dashboard" })
                        } else {
                            PracticeScreen(
                                frontText = card.frontText,
                                backText = card.backText,
                                onAnswerSelected = { q -> practiceViewModel.onUserAnswered(q) },
                                onSpeak = { speakOut(card.frontText) },
                                onExit = { currentScreen = "dashboard" }
                            )
                        }
                    }
                }
            }
        }
    }

    // Hàm tính toán và hẹn giờ 17:00
    private fun setupDailyReminder() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 17)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = calendar.timeInMillis - now

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun sendCongratulationNotification(deckName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "study_done_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hoàn thành", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tuyệt vời! 🎉")
            .setContentText(deckName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
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


@Composable
fun ResultScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chúc mừng!\nBạn đã hoàn thành bài học.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) { Text("Quay về Dashboard") }
    }
}