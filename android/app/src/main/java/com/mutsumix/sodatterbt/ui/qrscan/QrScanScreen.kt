package com.mutsumix.sodatterbt.ui.qrscan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// cultivationId == -1L: カメラでQR手動スキャン
// cultivationId >= 0L: ディープリンク経由でIDが確定済み (sodatterbt://cultivation/{id})
@Composable
fun QrScanScreen(
    cultivationId: Long,
    onNavigateToPhotoRecord: (deviceId: Int) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val label = if (cultivationId >= 0L) "QRスキャン（ID=$cultivationId）" else "QRスキャン（手動）"
        Text("TODO: $label")
    }
}
