package com.mutsumix.sodatterbt.ui.photorecord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val Surface2 = Color(0xFFF7F7F7)

private data class RecentLog(val date: String)

private data class DevicePhotoInfo(
    val cropName: String,
    val daysElapsed: Int,
    val recentLogs: List<RecentLog>,
)

private val mockDevices = mapOf(
    0 to DevicePhotoInfo(
        cropName = "サニーレタス",
        daysElapsed = 32,
        recentLogs = listOf(RecentLog("01/20"), RecentLog("02/01"), RecentLog("02/10")),
    ),
    1 to DevicePhotoInfo(
        cropName = "バジル",
        daysElapsed = 15,
        recentLogs = emptyList(),
    ),
)

@Composable
fun PhotoRecordScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val device = mockDevices[deviceId % mockDevices.size] ?: mockDevices[0]!!
    val deviceLabel = ('A' + (deviceId % 4)).toString()
    var isPreview by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Simulated camera background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A2A1A)),
        )

        if (isPreview) {
            // Preview screen
            PreviewOverlay(
                onRetake = { isPreview = false },
                onSave = {
                    showToast = true
                    onSaved()
                },
            )
        } else {
            // Viewfinder screen
            ViewfinderInfoBar(
                deviceLabel = deviceLabel,
                cropName = device.cropName,
                daysElapsed = device.daysElapsed,
            )

            if (device.recentLogs.isNotEmpty()) {
                ReferenceThumbnailStrip(
                    logs = device.recentLogs.takeLast(3),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            ViewfinderControls(
                onSkip = onBack,
                onShutter = { isPreview = true },
            )
        }

        if (showToast) {
            SaveToast()
        }
    }
}

@Composable
private fun ViewfinderInfoBar(
    deviceLabel: String,
    cropName: String,
    daysElapsed: Int,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, Secondary),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "デバイス $deviceLabel — $cropName",
                        color = OnBackground,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${daysElapsed}日目",
                        color = Primary,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceThumbnailStrip(
    logs: List<RecentLog>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 108.dp, start = 16.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            logs.forEach { log ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Surface2,
                        border = BorderStroke(1.dp, Divider),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌱", fontSize = 16.sp)
                        }
                    }
                    Text(
                        text = log.date,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewfinderControls(
    onSkip: () -> Unit,
    onShutter: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                    ),
                ),
        ) {
            // Skip button - left
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp),
            ) {
                Text("スキップ", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }

            // Shutter button - center
            Box(modifier = Modifier.align(Alignment.Center)) {
                ShutterButton(onClick = onShutter)
            }
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(64.dp),
        border = BorderStroke(2.dp, Secondary),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = OutlinedButtonDefaults.outlinedButtonColors(containerColor = Color.White),
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.5.dp, Divider),
            modifier = Modifier.size(52.dp),
        ) {}
    }
}

@Composable
private fun PreviewOverlay(
    onRetake: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.70f)),
                    ),
                )
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Divider),
                colors = OutlinedButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            ) {
                Text("撮り直す", color = Color.White, fontSize = 16.sp)
            }
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Secondary),
                colors = OutlinedButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            ) {
                Text("保存", color = Secondary, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SaveToast() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OnBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 112.dp),
        ) {
            Text(
                text = "写真を保存しました",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
