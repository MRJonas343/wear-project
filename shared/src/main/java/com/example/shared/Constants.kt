package com.example.shared

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ColorConstants {
    val PrimaryBlue = Color(0xFF4285F4)
    val HeaderGradient = Brush.horizontalGradient(listOf(Color(0xFF4285F4), Color(0xFFA56EFF)))
    val ReminderGradient = Brush.horizontalGradient(listOf(Color(0xFFFF8A00), Color(0xFFFF4057)))
    val BackgroundGray = Color(0xFFF5F5F5)
    val SuccessGreen = Color(0xFF00C853)
    val AlertOrange = Color(0xFFFF9800)
    val ErrorRed = Color(0xFFFF1744)
}