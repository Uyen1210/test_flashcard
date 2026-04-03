package com.example.test_flashcrad.ui.theme.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FlashcardItem(
    frontText: String,
    backText: String,
    onSpeak: () -> Unit // Thêm hàm phát âm vào đây
) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(500),
        label = "flip"
    )
    val isBack = rotation > 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp) // Tăng nhẹ chiều cao để chứa nút loa
            .padding(16.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { rotated = !rotated },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isBack) {
                // MẶT SAU: Có chữ và nút Loa
                Column(
                    modifier = Modifier
                        .graphicsLayer { rotationY = 180f }
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = backText,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút loa to rõ nét
                    IconButton(
                        onClick = { onSpeak() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            } else {
                // MẶT TRƯỚC
                Text(
                    text = frontText,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PracticeScreen(
    frontText: String,
    backText: String,
    onAnswerSelected: (Int) -> Unit,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gọi FlashcardItem và truyền hàm onSpeak vào
        FlashcardItem(frontText = frontText, backText = backText, onSpeak = onSpeak)

        Spacer(modifier = Modifier.height(40.dp))

        Text(text = "Mức độ ghi nhớ của bạn?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onAnswerSelected(0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))) {
                Text("Quên")
            }
            Button(onClick = { onAnswerSelected(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))) {
                Text("Khó")
            }
            Button(onClick = { onAnswerSelected(2) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))) {
                Text("Vừa")
            }
            Button(onClick = { onAnswerSelected(3) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))) {
                Text("Dễ")
            }
        }
    }
}