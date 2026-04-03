package com.example.test_flashcrard.ui.theme.practice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_flashcard.data.Deck

@Composable
fun AddCardDialog(
    decks: List<Deck>, // Truyền danh sách bộ bài vào để chọn
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit // Trả về thêm deckId của bộ bài được chọn
) {
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }

    // Trạng thái quản lý việc đóng/mở menu và bộ bài được chọn
    var expanded by remember { mutableStateOf(false) }
    var selectedDeck by remember { mutableStateOf(decks.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thẻ mới") },
        text = {
            Column {
                // Phần chọn Bộ bài (Mục)
                Text("Thêm vào mục:", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedDeck?.name ?: "Chọn bộ bài")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        decks.forEach { deck ->
                            DropdownMenuItem(
                                text = { Text(deck.name) },
                                onClick = {
                                    selectedDeck = deck
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
                    // Trả về dữ liệu kèm theo ID của bộ bài đã chọn[cite: 1]
                    onConfirm(frontText, backText, selectedDeck!!.id)
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