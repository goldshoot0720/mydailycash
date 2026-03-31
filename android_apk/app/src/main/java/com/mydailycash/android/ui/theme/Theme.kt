package com.mydailycash.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB35A1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = Color(0xFF3F1A00),
    secondary = Color(0xFF7A6455),
    background = Color(0xFFFFF8F2),
    surface = Color(0xFFFFFBF8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB784),
    onPrimary = Color(0xFF5E2A00),
    primaryContainer = Color(0xFF874012),
    onPrimaryContainer = Color(0xFFFFDCC4),
    secondary = Color(0xFFE7C1A9),
    background = Color(0xFF1B1410),
    surface = Color(0xFF241C17),
)

@Composable
fun MyDailyCashAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
