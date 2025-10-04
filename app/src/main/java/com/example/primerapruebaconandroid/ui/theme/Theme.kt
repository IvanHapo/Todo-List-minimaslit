package com.example.primerapruebaconandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = Black,
    surface = DarkGray,
    onBackground = White,
    onSurface = White,
    primary = White,
    onPrimary = Black,
    secondary = MediumGray,
    onSecondary = White
)


@Composable
fun PrimeraPruebaConAndroidTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}