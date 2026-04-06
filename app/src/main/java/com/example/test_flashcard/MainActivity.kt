package com.example.test_flashcard

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.test_flashcard.ui.theme.Test_flashcardTheme
import com.example.test_flashcard.ui.theme.practice.*
import com.example.test_flashcard.worker.ReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.app.PendingIntent

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupDailyReminder(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        setContent {
            Test_flashcardTheme {
                MainScreenContent(
                    onSpeak = { text -> speakOut(text) },
                    onSendNotification = { msg -> sendCongratulationNotification(msg) }
                )
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setupDailyReminder(this)
            }
        } else {
            setupDailyReminder(this)
        }
    }

    private fun sendCongratulationNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "study_done_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hoàn thành bài học", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Chúc mừng! 🎉")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
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
fun MainScreenContent(
    onSpeak: (String) -> Unit,
    onSendNotification: (String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val practiceViewModel: PracticeViewModel = viewModel(
        factory = PracticeViewModelFactory(application.repository)
    )

    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedDeckIdForImport by remember { mutableIntStateOf(-1) }
    var activeDeckId by remember { mutableIntStateOf(-1) }

    var manageDeckId by remember { mutableIntStateOf(-1) }
    var manageDeckName by remember { mutableStateOf("") }

    val currentCard by practiceViewModel.currentCard.collectAsState()
    val decksWithProgress by practiceViewModel.decksWithProgress.collectAsState(initial = emptyList())
    val streak by practiceViewModel.streakCount.collectAsState()
    val totalLearned = decksWithProgress.sumOf { it.learnedCards }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedDeckIdForImport != -1) {
                practiceViewModel.importCsv(it, context.contentResolver, selectedDeckIdForImport)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "dashboard" -> {
                DashboardScreen(
                    decks = decksWithProgress,
                    totalLearned = totalLearned,
                    streak = streak,
                    onDeckClick = { deckId ->
                        activeDeckId = deckId
                        practiceViewModel.startPractice(deckId)
                        currentScreen = "practice"
                    },
                    onManageCards = { deckId, deckName ->
                        manageDeckId = deckId
                        manageDeckName = deckName
                        currentScreen = "manage_cards"
                    },
                    onAddCard = { f, b, id -> practiceViewModel.addNewCard(f, b, id) },
                    onAddDeck = { n, c -> practiceViewModel.addNewDeck(n, c) },
                    onResetDeck = { id -> practiceViewModel.resetAllCardsInDeck(id) },
                    onImportClick = { id ->
                        selectedDeckIdForImport = id
                        filePickerLauncher.launch("text/*")
                    },
                    onDeleteDeck = { deck -> practiceViewModel.deleteDeck(deck) }
                )
            }
            "practice" -> {
                val card = currentCard
                if (card == null) {
                    LaunchedEffect(Unit) {
                        if (activeDeckId != -1) {
                            practiceViewModel.saveStudyLog(activeDeckId, 1)
                            val finishedDeck = decksWithProgress.find { it.deck.id == activeDeckId }
                            val deckName = finishedDeck?.deck?.name ?: "Bộ bài"
                            onSendNotification("Bạn đã hoàn thành bộ bài: $deckName")
                        }
                    }
                    ResultScreen(
                        onBack = { currentScreen = "dashboard" },
                        onRetry = {
                            if (activeDeckId != -1) {
                                practiceViewModel.startPractice(activeDeckId)
                            }
                        }
                    )
                } else {
                    PracticeScreen(
                        frontText = card.frontText,
                        backText = card.backText,
                        onAnswerSelected = { q -> practiceViewModel.onUserAnswered(q) },
                        onSpeak = { onSpeak(card.frontText) },
                        onExit = { currentScreen = "dashboard" }
                    )
                }
            }
            "manage_cards" -> {
                val cardsInDeck by practiceViewModel.getCardsByDeck(manageDeckId).collectAsState(initial = emptyList())
                ManageCardsScreen(
                    deckName = manageDeckName,
                    cards = cardsInDeck,
                    onDeleteCard = { card -> practiceViewModel.deleteCard(card) },
                    onUpdateCard = { card, f, b -> practiceViewModel.updateCardContent(card, f, b) },
                    onBack = { currentScreen = "dashboard" }
                )
            }
        }
    }
}

fun setupDailyReminder(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_reminder",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}

@Composable
fun ResultScreen(onBack: () -> Unit, onRetry: () -> Unit) {
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
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Học lại bộ bài này")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Quay về Dashboard")
        }
    }
}