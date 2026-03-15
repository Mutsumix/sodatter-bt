package com.mutsumix.sodatterbt.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
private val Surface2 = Color(0xFFF7F7F7)

private data class GrowthLog(val date: String)

private data class DeviceDetail(
    val id: String,
    val cropName: String,
    val manufacturer: String,
    val seedingDate: String,
    val daysElapsed: Int,
    val growthLogs: List<GrowthLog>,
)

private val mockDevices = mapOf(
    0 to DeviceDetail(
        id = "A",
        cropName = "ミニトマト",
        manufacturer = "タキイ種苗",
        seedingDate = "2026/01/04",
        daysElapsed = 32,
        growthLogs = listOf(
            GrowthLog("01/06"),
            GrowthLog("01/12"),
            GrowthLog("01/20"),
            GrowthLog("02/01"),
        ),
    ),
    1 to DeviceDetail(
        id = "B",
        cropName = "バジル",
        manufacturer = "サカタのタネ",
        seedingDate = "2026/02/01",
        daysElapsed = 15,
        growthLogs = emptyList(),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onHarvestClick: () -> Unit,
) {
    val device = mockDevices[deviceId % mockDevices.size] ?: mockDevices[0]!!

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = device.cropName,
                        color = OnBackground,
                        fontSize = 20.sp,
                    )
                },
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
            HarvestBottomBar(onHarvestClick = onHarvestClick)
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
            InfoCard(device = device)
            GrowthLogSection(device = device)
        }
    }
}

@Composable
private fun InfoCard(device: DeviceDetail) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Secondary),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Surface2,
                    border = BorderStroke(1.dp, Divider),
                    modifier = Modifier.size(80.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🌱", fontSize = 32.sp)
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DeviceSlotBadge(label = device.id)
                        Text(device.cropName, color = OnBackground, fontSize = 16.sp)
                    }
                    Text(device.manufacturer, color = Muted, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("播種日：${device.seedingDate}", color = Muted, fontSize = 14.sp, modifier = Modifier.weight(1f))
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
}

@Composable
private fun GrowthLogSection(device: DeviceDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "生育ログ")
        if (device.growthLogs.isEmpty()) {
            GrowthLogEmpty()
        } else {
            GrowthLogStrip(logs = device.growthLogs)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = OnBackground, fontSize = 16.sp)
    }
}

@Composable
private fun GrowthLogEmpty() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Surface2,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "まだ写真がありません。\nデバイスタグのQRをスキャンして追加してください。",
                color = Muted,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun GrowthLogStrip(logs: List<GrowthLog>) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        logs.forEach { log ->
            GrowthLogThumbnail(log = log)
        }
    }
}

@Composable
private fun GrowthLogThumbnail(log: GrowthLog) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Surface2,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🌱", fontSize = 20.sp)
            }
        }
        Text(log.date, color = Muted, fontSize = 10.sp)
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
private fun HarvestBottomBar(onHarvestClick: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = Divider)
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = onHarvestClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Secondary),
                ) {
                    Text("✂", color = Secondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("収穫", color = Secondary, fontSize = 16.sp)
                }
            }
        }
    }
}
