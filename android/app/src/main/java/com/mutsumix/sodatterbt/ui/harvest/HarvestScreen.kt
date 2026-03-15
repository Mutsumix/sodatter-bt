package com.mutsumix.sodatterbt.ui.harvest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HarvestScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onLabelPrintClick: () -> Unit,
    onComplete: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("TODO: 収穫記録（装置$deviceId）")
    }
}
