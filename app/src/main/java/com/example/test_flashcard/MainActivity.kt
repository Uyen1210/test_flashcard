package com.example.test_flashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_flashcard.ui.theme.Test_flashcardTheme
import com.example.test_flashcard.ui.theme.practice.DashboardScreen
import com.example.test_flashcard.ui.theme.practice.PracticeScreen
import com.example.test_flashcard.ui.theme.practice.PracticeViewModel
import com.example.test_flashcard.ui.theme.practice.PracticeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_flashcardTheme {
                val application = LocalContext.current.applicationContext as MyApplication
                val practiceViewModel: PracticeViewModel = viewModel(
                    factory = PracticeViewModelFactory(application.repository)
                )

                // Biến điều hướng màn hình
                var currentScreen by remember { mutableStateOf("dashboard") }

                // Lắng nghe dữ liệu từ ViewModel
                val currentCard by practiceViewModel.currentCard.collectAsState()
                val decks by practiceViewModel.allDecks.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentScreen == "dashboard") {
                        // MÀN HÌNH TRANG CHỦ - Phải đủ 4 tham số
                        // Trong MainActivity.kt
                        DashboardScreen(
                            decks = decks,
                            onDeckClick = { id ->
                                practiceViewModel.startPractice(id)
                                currentScreen = "practice"
                            },
                            onAddCard = { f, b, id -> practiceViewModel.addNewCard(f, b, id) },
                            onAddDeck = { name, cat -> practiceViewModel.addNewDeck(name, cat) } // Thêm dòng này
                        )
                    } else if (currentScreen == "practice") {
                        // MÀN HÌNH HỌC TẬP
                        val card = currentCard
                        if (card == null) {
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
                                Button(onClick = { currentScreen = "dashboard" }) {
                                    Text("Quay về Trang chủ")
                                }
                            }
                        } else {
                            PracticeScreen(
                                frontText = card.frontText,
                                backText = card.backText,
                                onAnswerSelected = { quality ->
                                    practiceViewModel.onUserAnswered(quality)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}