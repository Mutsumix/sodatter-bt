package com.mutsumix.sodatterbt.ui.qrscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)

// cultivationId == -1L: カメラでQR手動スキャン
// cultivationId >= 0L: ディープリンク経由でIDが確定済み (sodatterbt://cultivation/{id})
@Composable
fun QrScanScreen(
    cultivationId: Long,
    onNavigateToPhotoRecord: (deviceId: Int) -> Unit,
    onBack: () -> Unit,
) {
    if (cultivationId >= 0L) {
        QrConfirmedScreen(
            cultivationId = cultivationId,
            onNavigateToPhotoRecord = onNavigateToPhotoRecord,
            onBack = onBack,
        )
    } else {
        QrScanningScreen(
            onBack = onBack,
        )
    }
}

@Composable
private fun QrScanningScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Simulated camera background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A2A1A)),
        )

        // Top overlay with instruction text
        TopScanOverlay()

        // Scan frame - centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ScanFrame(isDetected = false)
        }

        // Bottom overlay with cancel button
        BottomScanOverlay(
            isScanning = true,
            onCancel = onBack,
        )
    }
}

@Composable
private fun QrConfirmedScreen(
    cultivationId: Long,
    onNavigateToPhotoRecord: (deviceId: Int) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A2A1A)),
        )

        TopScanOverlay()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ScanFrame(isDetected = true)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(
                    Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                ),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ID: $cultivationId",
                    color = Color.White,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onNavigateToPhotoRecord(cultivationId.toInt()) }) {
                    Text("写真記録へ進む", color = Secondary, fontSize = 16.sp)
                }
                TextButton(onClick = onBack) {
                    Text("キャンセル", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun TopScanOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.72f),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = 400f,
                        ),
                    ),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = "デバイスのタグをスキャンしてください",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = androidx.compose.ui.Modifier.padding(top = 60.dp, start = 32.dp, end = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun ScanFrame(isDetected: Boolean) {
    val frameColor = if (isDetected) Secondary else Primary
    val cornerLength = 28.dp

    Box(
        modifier = Modifier
            .size(220.dp)
            .drawWithContent {
                drawContent()
                val stroke = Stroke(width = 2.dp.toPx())
                val cl = cornerLength.toPx()
                val w = size.width
                val h = size.height

                // Top-left
                drawLine(frameColor, Offset(0f, cl), Offset(0f, 0f), stroke.width)
                drawLine(frameColor, Offset(0f, 0f), Offset(cl, 0f), stroke.width)
                // Top-right
                drawLine(frameColor, Offset(w - cl, 0f), Offset(w, 0f), stroke.width)
                drawLine(frameColor, Offset(w, 0f), Offset(w, cl), stroke.width)
                // Bottom-left
                drawLine(frameColor, Offset(0f, h - cl), Offset(0f, h), stroke.width)
                drawLine(frameColor, Offset(0f, h), Offset(cl, h), stroke.width)
                // Bottom-right
                drawLine(frameColor, Offset(w - cl, h), Offset(w, h), stroke.width)
                drawLine(frameColor, Offset(w, h - cl), Offset(w, h), stroke.width)

                if (isDetected) {
                    drawRoundRect(
                        color = Secondary.copy(alpha = 0.08f),
                        size = size,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawRoundRect(
                        color = Secondary,
                        size = size,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            },
    ) {}
}

@Composable
private fun BottomScanOverlay(
    isScanning: Boolean,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        startY = 400f,
                    ),
                )
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (isScanning) {
                TextButton(onClick = onCancel) {
                    Text("キャンセル", color = Color.White, fontSize = 16.sp)
                }
            } else {
                Text("QRコードを検出しました…", color = Color.White, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
