package com.mutsumix.sodatterbt.ui.harvest

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

private data class DeviceHarvestInfo(
    val id: String,
    val cropName: String,
    val manufacturer: String,
    val seedingDate: String,
    val daysElapsed: Int,
)

private val mockDevices = mapOf(
    0 to DeviceHarvestInfo("A", "ミニトマト", "タキイ種苗", "2026/01/04", 42),
    1 to DeviceHarvestInfo("B", "バジル", "サカタのタネ", "2026/02/01", 15),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onLabelPrintClick: () -> Unit,
    onComplete: () -> Unit,
) {
    val device = mockDevices[deviceId % mockDevices.size] ?: mockDevices[0]!!
    var weight by remember { mutableStateOf<Double?>(142.5) }
    var scaleConnected by remember { mutableStateOf(true) }
    var harvestDate by remember { mutableStateOf("2026-03-15") }
    var completed by remember { mutableStateOf(false) }

    if (completed) {
        HarvestCompleteScreen(
            device = device,
            weight = weight,
            harvestDate = harvestDate,
            onGoHome = onComplete,
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("収穫", color = OnBackground, fontSize = 20.sp) },
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
            HarvestBottomBar(
                onComplete = { completed = true },
                onLabelPrint = onLabelPrintClick,
            )
        },
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CropInfoCard(device = device)
            WeightDisplay(
                weight = weight,
                scaleConnected = scaleConnected,
                onTare = { weight = 0.0 },
            )
            HarvestDateField(value = harvestDate)
        }
    }
}

@Composable
private fun HarvestCompleteScreen(
    device: DeviceHarvestInfo,
    weight: Double?,
    harvestDate: String,
    onGoHome: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Secondary),
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✂", color = Secondary, fontSize = 28.sp)
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("収穫が完了しました", color = OnBackground, fontSize = 18.sp)
                Text("デバイス ${device.id}（${device.cropName}）", color = Muted, fontSize = 14.sp)
                if (weight != null && weight > 0) {
                    Text("収穫量：${weight} g", color = Muted, fontSize = 14.sp)
                }
                val parts = harvestDate.split("-")
                val display = if (parts.size == 3) "${parts[0]}/${parts[1]}/${parts[2]}" else harvestDate
                Text("収穫日：$display", color = Muted, fontSize = 14.sp)
            }
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Primary),
            ) {
                Text("ホームに戻る", color = Primary, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun CropInfoCard(device: DeviceHarvestInfo) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Divider),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeviceSlotBadge(label = device.id)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                device.cropName,
                color = OnBackground,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 12.dp),
                color = Divider,
            )
            Text("播種日 ${device.seedingDate}", color = Muted, fontSize = 14.sp)
            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 12.dp),
                color = Divider,
            )
            Text("Day ${device.daysElapsed}", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WeightDisplay(
    weight: Double?,
    scaleConnected: Boolean,
    onTare: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val weightText = when {
                weight == null -> "---"
                weight % 1.0 == 0.0 -> "${weight.toInt()}.0"
                else -> "$weight"
            }
            Text(
                text = weightText,
                color = OnBackground,
                fontSize = 48.sp,
            )
            Text(
                text = "g",
                color = Muted,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            OutlinedButton(
                onClick = onTare,
                modifier = Modifier
                    .height(28.dp)
                    .padding(bottom = 4.dp),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Primary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            ) {
                Text("風袋引き", color = Primary, fontSize = 12.sp)
            }
        }
        StatusDot(
            connected = scaleConnected,
            connectedLabel = "Decent Scale：接続済み",
            disconnectedLabel = "未接続",
        )
    }
}

@Composable
private fun HarvestDateField(value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("収穫日", color = OnBackground, fontSize = 14.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val parts = value.split("-")
                val display = if (parts.size == 3) "${parts[0]}/${parts[1]}/${parts[2]}" else value
                Text(display, color = OnBackground, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun StatusDot(
    connected: Boolean,
    connectedLabel: String,
    disconnectedLabel: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = if (connected) Secondary else Divider,
            modifier = Modifier.size(8.dp),
        ) {}
        Text(
            text = if (connected) connectedLabel else disconnectedLabel,
            color = if (connected) Muted else Color(0xFFABABAB),
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun DeviceSlotBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Secondary),
        modifier = Modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Secondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HarvestBottomBar(
    onComplete: () -> Unit,
    onLabelPrint: () -> Unit,
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
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Secondary),
                ) {
                    Text("✂", color = Secondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("収穫を完了する", color = Secondary, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = onLabelPrint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Primary),
                ) {
                    Text("🖨", color = Primary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ラベルを印刷", color = Primary, fontSize = 16.sp)
                }
            }
        }
    }
}
