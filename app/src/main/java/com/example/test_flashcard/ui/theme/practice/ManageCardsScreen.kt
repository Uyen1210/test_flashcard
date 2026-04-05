package com.example.test_flashcard.ui.theme.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_flashcard.data.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(
    deckName: String,
    cards: List<Flashcard>,
    onDeleteCard: (Flashcard) -> Unit,
    onUpdateCard: (Flashcard, String, String) -> Unit,
    onBack: () -> Unit
) {
    var editingCard by remember { mutableStateOf<Flashcard?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý: $deckName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Bộ bài này chưa có thẻ nào.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Mặt trước: ${card.frontText}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Mặt sau: ${card.backText}", style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { editingCard = card }) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteCard(card) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        editingCard?.let { card ->
            EditCardDialog(
                card = card,
                onDismiss = { editingCard = null },
                onConfirm = { front, back ->
                    onUpdateCard(card, front, back)
                    editingCard = null
                }
            )
        }
    }
}

@Composable
fun EditCardDialog(
    card: Flashcard,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var front by remember { mutableStateOf(card.frontText) }
    var back by remember { mutableStateOf(card.backText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa thẻ bài") },
        text = {
            Column {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Mặt trước") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Mặt sau") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(front, back) }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}