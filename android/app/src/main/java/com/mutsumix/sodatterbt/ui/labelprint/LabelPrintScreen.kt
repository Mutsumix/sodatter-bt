package com.mutsumix.sodatterbt.ui.labelprint

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

private data class DeviceLabelInfo(
    val id: String,
    val cropName: String,
    val manufacturer: String,
    val seedingDate: String,
    val harvestDate: String,
    val weight: Double,
    val daysElapsed: Int,
)

private val mockDevices = mapOf(
    0 to DeviceLabelInfo("A", "ミニトマト", "タキイ種苗", "2026/01/04", "2026/02/15", 142.5, 42),
    1 to DeviceLabelInfo("B", "バジル", "サカタのタネ", "2026/02/01", "2026/03/18", 87.0, 45),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPrintScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val device = mockDevices[deviceId % mockDevices.size] ?: mockDevices[0]!!
    var printerConnected by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ラベルプレビュー", color = OnBackground, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                            tint = Muted,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        bottomBar = {
            LabelPrintBottomBar(
                printerConnected = printerConnected,
                onPrint = { /* mock print */ },
                onDone = onDone,
            )
        },
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LabelMockup(device = device)
            PrinterStatusCard(
                connected = printerConnected,
                connecting = connecting,
                onConnect = {
                    connecting = true
                    printerConnected = true
                    connecting = false
                },
            )
        }
    }
}

@Composable
private fun LabelMockup(device: DeviceLabelInfo) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier.wrapContentWidth(),
        ) {
            Column {
                PerforatedEdge()
                Column(modifier = Modifier.padding(20.dp)) {
                    // App name watermark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("🌿", fontSize = 14.sp)
                        Text(
                            "Sodatter-BT",
                            color = Color(0xFFABABAB),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Crop name
                    Text(device.cropName, color = OnBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(device.manufacturer, color = Muted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dates
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LabelDateRow(icon = "🌱", label = "播種：", value = device.seedingDate)
                        LabelDateRow(icon = "✂", label = "収穫：", value = device.harvestDate)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Weight + QR row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("重量", color = Muted, fontSize = 12.sp)
                            val weightText = if (device.weight % 1.0 == 0.0) "${device.weight.toInt()}.0" else "${device.weight}"
                            Text(weightText, color = OnBackground, fontSize = 36.sp)
                            Text("g", color = Muted, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                DeviceSlotBadge(label = device.id, size = 20)
                                Text("Day ${device.daysElapsed}", color = Muted, fontSize = 12.sp)
                            }
                        }
                        QrCodeMockup()
                    }
                }
                PerforatedEdge()
            }
        }
        Text(
            "QRコードは生育フォトログにリンクします",
            color = Muted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun PerforatedEdge() {
    val dashColor = Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .drawBehind {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                drawLine(
                    color = dashColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    pathEffect = dashEffect,
                    strokeWidth = 1.dp.toPx(),
                )
            },
    )
}

@Composable
private fun LabelDateRow(icon: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(icon, fontSize = 12.sp)
        Text(label, color = OnBackground, fontSize = 14.sp)
        Text(value, color = OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QrCodeMockup() {
    // Simple mock QR code using a grid
    val pattern = listOf(
        listOf(1, 1, 1, 0, 1, 1, 1),
        listOf(1, 0, 1, 0, 1, 0, 1),
        listOf(1, 1, 1, 0, 1, 1, 1),
        listOf(0, 0, 0, 1, 0, 0, 0),
        listOf(1, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 0, 1, 0, 0, 1),
        listOf(1, 1, 1, 0, 1, 1, 1),
    )
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Divider),
        modifier = Modifier.padding(4.dp),
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            pattern.forEach { row ->
                Row {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(0.5.dp),
                        ) {
                            Surface(
                                color = if (cell == 1) OnBackground else Color.White,
                                modifier = Modifier.fillMaxSize(),
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrinterStatusCard(
    connected: Boolean,
    connecting: Boolean,
    onConnect: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Divider),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Primary),
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🖨", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Star SM-S210i",
                    color = OnBackground,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatusDot(connected = connected)
                    Text(
                        if (connected) "接続済み" else "未接続",
                        color = if (connected) Muted else Color(0xFFABABAB),
                        fontSize = 12.sp,
                    )
                }
            }
            if (!connected) {
                OutlinedButton(
                    onClick = onConnect,
                    enabled = !connecting,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(if (connecting) "接続中…" else "接続する", color = Primary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusDot(connected: Boolean) {
    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = if (connected) Secondary else Divider,
        modifier = Modifier.size(8.dp),
    ) {}
}

@Composable
private fun DeviceSlotBadge(label: String, size: Int = 24) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Secondary),
        modifier = Modifier.size(size.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Secondary, fontSize = (size * 0.5f).sp)
        }
    }
}

@Composable
private fun LabelPrintBottomBar(
    printerConnected: Boolean,
    onPrint: () -> Unit,
    onDone: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = Divider)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onPrint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Primary),
                ) {
                    Text("🖨", color = Primary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("印刷", color = Primary, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Secondary),
                ) {
                    Text("🏠", color = Secondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("完了 — ホームに戻る", color = Secondary, fontSize = 16.sp)
                }
            }
        }
    }
}
