package com.mutsumix.sodatterbt.ui.seeding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SeedingScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("TODO: 播種登録（装置$deviceId）")
    }
}
