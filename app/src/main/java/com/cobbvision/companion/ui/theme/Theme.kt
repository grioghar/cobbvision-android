package com.cobbvision.companion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CobbOrange = Color(0xFFFF6B00)
private val CobbDark   = Color(0xFF121212)

private val DarkColors = darkColorScheme(
    primary        = CobbOrange,
    onPrimary      = Color.Black,
    secondary      = Color(0xFFFFAB40),
    background     = CobbDark,
    surface        = Color(0xFF1E1E1E),
    onBackground   = Color.White,
    onSurface      = Color.White,
)

@Composable
fun CobbVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content     = content,
    )
}
