package com.example.test_flashcard.ui.theme.practice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_flashcard.data.DeckWithProgress

@Composable
fun AddCardDialog(
    decks: List<DeckWithProgress>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    // selectedDeck bây giờ là một đối tượng DeckWithProgress
    var selectedDeck by remember { mutableStateOf(decks.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thẻ mới") },
        text = {
            Column {
                Text("Thêm vào mục:", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedDeck?.deck?.name ?: "Chọn bộ bài")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        decks.forEach { item ->
                            DropdownMenuItem(
                                // FIX: Truy cập vào item.deck.name
                                text = { Text(item.deck.name) },
                                onClick = {
                                    selectedDeck = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = frontText,
                    onValueChange = { frontText = it },
                    label = { Text("Mặt trước (Câu hỏi)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backText,
                    onValueChange = { backText = it },
                    label = { Text("Mặt sau (Đáp án)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (frontText.isNotBlank() && backText.isNotBlank() && selectedDeck != null) {
                    onConfirm(frontText, backText, selectedDeck!!.deck.id)
                }
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}