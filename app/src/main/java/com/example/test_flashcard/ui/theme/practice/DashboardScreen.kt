package com.example.test_flashcard.ui.theme.practice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.test_flashcard.data.DeckWithProgress
import com.example.test_flashcard.ui.theme.practice.AddCardDialog
import kotlin.collections.isNotEmpty
import androidx.compose.ui.graphics.Color
import com.example.test_flashcard.data.Deck

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    decks: List<DeckWithProgress>,
    totalLearned: Int,
    streak: Int,
    onDeckClick: (Int) -> Unit,
    onManageCards: (Int, String) -> Unit,
    onAddCard: (String, String, Int) -> Unit,
    onAddDeck: (String, String) -> Unit,
    onDeleteDeck: (Deck) -> Unit, // Thêm callback xóa bộ bài
    onResetDeck: (Int) -> Unit,
    onImportClick: (Int) -> Unit
) {
    var showCardDialog by remember { mutableStateOf(false) }
    var showDeckDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // State cho dialog xóa
    var deckToProcess by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Flashcard App")
                        if (streak > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("🔥 $streak", color = Color(0xFFFF5722), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tổng số thẻ đã thuộc",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "$totalLearned 🎓",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(text = "Các bộ bài của bạn:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (decks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu. Bấm nút Folder ở trên để tạo bộ bài!")
                }
            } else {
                LazyColumn {
                    items(decks) { item ->
                        val deck = item.deck

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .combinedClickable(
                                    onClick = { onDeckClick(deck.id) },
                                    onLongClick = {
                                        deckToProcess = deck
                                        showResetDialog = true
                                    }
                                ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = deck.name, style = MaterialTheme.typography.titleLarge)
                                        Text(text = deck.category, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Nút Quản lý thẻ
                                        IconButton(onClick = { onManageCards(deck.id, deck.name) }) {
                                            Icon(Icons.Default.List, contentDescription = "Quản lý thẻ", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        // Nút Xóa bộ bài
                                        IconButton(onClick = { 
                                            deckToProcess = deck
                                            showDeleteDialog = true 
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Xóa bộ bài", tint = MaterialTheme.colorScheme.error)
                                        }

                                        OutlinedButton(
                                            onClick = { onImportClick(deck.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Import CSV", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                LinearProgressIndicator(
                                    progress = { item.progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    strokeCap = StrokeCap.Round
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Tiến độ: ${item.progressText}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog Reset Tiến độ
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Học lại từ đầu?") },
                text = { Text("Bạn có muốn xóa toàn bộ tiến độ của bộ bài '${deckToProcess?.name}' không?") },
                confirmButton = {
                    Button(onClick = {
                        deckToProcess?.let { onResetDeck(it.id) }
                        showResetDialog = false
                    }) { Text("Xác nhận") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Hủy") }
                }
            )
        }

        // Dialog Xóa Bộ Bài
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xóa bộ bài?") },
                text = { Text("Hành động này sẽ xóa vĩnh viễn bộ bài '${deckToProcess?.name}' và tất cả các thẻ bên trong. Bạn có chắc chắn không?") },
                confirmButton = {
                    Button(
                        onClick = {
                            deckToProcess?.let { onDeleteDeck(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Xóa vĩnh viễn") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
                }
            )
        }

        if (showDeckDialog) {
            AddDeckDialog(
                onDismiss = { showDeckDialog = false },
                onConfirm = { name, cat ->
                    onAddDeck(name, cat)
                    showDeckDialog = false
                }
            )
        }

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