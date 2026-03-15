package com.mutsumix.sodatterbt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// デザイン方針: デジタル庁デザインシステム準拠
// Primary: 淡い青 (#5B8BD4) — Bluetooth
// Secondary: 淡い緑 (#6DAE72) — 植物/成長
private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
)

@Composable
fun SodatterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
