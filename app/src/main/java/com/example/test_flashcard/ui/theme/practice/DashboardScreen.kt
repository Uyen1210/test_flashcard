package com.example.test_flashcard.ui.theme.practice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.test_flashcard.data.Deck
import com.example.test_flashcrard.ui.theme.practice.AddCardDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    decks: List<Deck>,
    onDeckClick: (Int) -> Unit,
    onAddCard: (String, String, Int) -> Unit,
    onAddDeck: (String, String) -> Unit,
    onResetDeck: (Int) -> Unit
) {
    var showCardDialog by remember { mutableStateOf(false) }
    var showDeckDialog by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var deckIdToReset by remember { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { showDeckDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Thêm bộ bài")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (decks.isNotEmpty()) showCardDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thẻ")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Các bộ bài của bạn:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (decks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu. Bấm nút Folder ở trên để tạo bộ bài!")
                }
            } else {
                LazyColumn {
                    items(decks) { deck ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth() // Thêm cái này cho Card dài ra
                                .padding(vertical = 8.dp)
                                .combinedClickable( // 3. Sửa lỗi dư dấu ngoặc ở đây
                                    onClick = { onDeckClick(deck.id) },
                                    onLongClick = {
                                        deckIdToReset = deck.id
                                        showResetDialog = true
                                    }
                                ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = deck.name, style = MaterialTheme.typography.titleLarge)
                                Text(text = deck.category, style = MaterialTheme.typography.bodyMedium)

                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { 0.4f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    strokeCap = StrokeCap.Round
                                )
                                Text(text = "Tiến độ: 40%", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Học lại từ đầu?") },
                text = { Text("Bạn có muốn xóa toàn bộ tiến độ của bộ bài này không?") },
                confirmButton = {
                    Button(onClick = {
                        onResetDeck(deckIdToReset)
                        showResetDialog = false
                    }) { Text("Xác nhận") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Hủy") }
                }
            )
        }

        // Dialog thêm bộ bài
        if (showDeckDialog) {
            AddDeckDialog(
                onDismiss = { showDeckDialog = false },
                onConfirm = { name, cat ->
                    onAddDeck(name, cat)
                    showDeckDialog = false
                }
            )
        }

        // Dialog thêm thẻ
        if (showCardDialog) {
            AddCardDialog(
                decks = decks,
                onDismiss = { showCardDialog = false },
                onConfirm = { front, back, deckId ->
                    onAddCard(front, back, deckId)
                    showCardDialog = false
                }
            )
        }
    }
}