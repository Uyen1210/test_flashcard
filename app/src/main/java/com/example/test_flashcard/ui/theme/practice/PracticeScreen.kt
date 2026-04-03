package com.example.test_flashcard.ui.theme.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FlashcardItem(frontText: String, backText: String) {
    // Biến trạng thái lưu trữ xem thẻ đang lật hay chưa
    var rotated by remember { mutableStateOf(false) }

    // Tạo hiệu ứng lật thẻ mượt mà (500 milliseconds)
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(500),
        label = "flipAnimation"
    )

    // Xử lý để chữ không bị ngược khi lật thẻ sang mặt sau
    val isBack = rotation > 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density // Tạo chiều sâu 3D khi lật
            }
            .clickable { rotated = !rotated }, // Chạm vào thẻ để lật
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Nếu góc xoay > 90 độ thì hiện mặt sau, ngược lại hiện mặt trước
            if (isBack) {
                Text(
                    text = backText,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { rotationY = 180f } // Xoay ngược chữ lại cho dễ đọc
                )
            } else {
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
    onAnswerSelected: (Int) -> Unit // Truyền mức độ khó (0-3) về cho ViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Hiển thị Thẻ nhớ
        FlashcardItem(frontText = frontText, backText = backText)

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Cụm 4 nút đánh giá SRS
        Text(text = "Mức độ ghi nhớ của bạn?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Nút 0: Quên sạch (Màu đỏ)
            Button(
                onClick = { onAnswerSelected(0) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                Text("Quên")
            }

            // Nút 1: Hơi khó (Màu cam)
            Button(
                onClick = { onAnswerSelected(1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
            ) {
                Text("Khó")
            }

            // Nút 2: Bình thường (Màu xanh dương)
            Button(
                onClick = { onAnswerSelected(2) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
            ) {
                Text("Vừa")
            }

            // Nút 3: Quá dễ (Màu xanh lá)
            Button(
                onClick = { onAnswerSelected(3) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
            ) {
                Text("Dễ")
            }
        }
    }
}