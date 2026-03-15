package com.mutsumix.sodatterbt.ui.labelprint

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LabelPrintScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("TODO: ラベル印刷プレビュー（装置$deviceId）")
    }
}
