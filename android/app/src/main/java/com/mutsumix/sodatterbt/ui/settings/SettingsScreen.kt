package com.mutsumix.sodatterbt.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val Surface2 = Color(0xFFF7F7F7)

private data class DeviceConfig(val id: String, val tag: String?)
private data class PeripheralStatus(val name: String, val connected: Boolean)

private val deviceConfigs = listOf(
    DeviceConfig("A", "AA:BB:CC:DD:EE:01"),
    DeviceConfig("B", "AA:BB:CC:DD:EE:02"),
    DeviceConfig("C", null),
    DeviceConfig("D", null),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(innerPadding: PaddingValues) {
    val peripherals = remember {
        mutableStateListOf(
            PeripheralStatus("Decent Scale", connected = true),
            PeripheralStatus("Star SM-S210i", connected = false),
        )
    }
    var licensesOpen by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                SettingsSection(title = "デバイス") {
                    DevicesSection(devices = deviceConfigs)
                }
            }
            item {
                SettingsSection(title = "周辺機器") {
                    PeripheralsSection(
                        peripherals = peripherals,
                        onToggleConnect = { idx ->
                            val p = peripherals[idx]
                            peripherals[idx] = p.copy(connected = !p.connected)
                        },
                    )
                }
            }
            item {
                SettingsSection(title = "データ") {
                    DataSection(
                        onExport = { showExportToast = true },
                    )
                }
            }
            item {
                SettingsSection(title = "このアプリについて") {
                    AboutSection(
                        onLicensesClick = { licensesOpen = true },
                    )
                }
            }
        }

        if (showExportToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    shape = CircleShape,
                    color = OnBackground.copy(alpha = 0.8f),
                ) {
                    Text(
                        "データをエクスポートしました",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }

    if (licensesOpen) {
        Dialog(
            onDismissRequest = { licensesOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
            ) {
                LicensesScreen(onBack = { licensesOpen = false })
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(text = title)
        content()
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Muted,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun DevicesSection(devices: List<DeviceConfig>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            devices.forEachIndexed { idx, device ->
                DeviceRow(device = device)
                if (idx < devices.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceConfig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeviceSlotBadge(label = device.id)
            Column {
                Text("デバイス ${device.id}", color = OnBackground, fontSize = 14.sp)
                if (device.tag != null) {
                    Text(
                        "タグ: ${device.tag}",
                        color = Muted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                } else {
                    Text("未割り当て", color = Color(0xFFABABAB), fontSize = 12.sp)
                }
            }
        }
        Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
    }
}

@Composable
private fun PeripheralsSection(
    peripherals: List<PeripheralStatus>,
    onToggleConnect: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            PeripheralRow(
                icon = "⚖",
                name = peripherals[0].name,
                connected = peripherals[0].connected,
                onConnect = { onToggleConnect(0) },
            )
            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            PeripheralRow(
                icon = "🖨",
                name = peripherals[1].name,
                connected = peripherals[1].connected,
                onConnect = { onToggleConnect(1) },
            )
            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Esp32Row(ip = "192.168.4.1")
        }
    }
}

@Composable
private fun PeripheralRow(
    icon: String,
    name: String,
    connected: Boolean,
    onConnect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 16.sp, color = Muted)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = OnBackground, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusDot(connected = connected)
                Text(
                    if (connected) "接続済み" else "未接続",
                    color = if (connected) Secondary else Color(0xFFABABAB),
                    fontSize = 12.sp,
                )
                if (!connected) {
                    TextButton(
                        onClick = onConnect,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text("接続する", color = Primary, fontSize = 12.sp)
                    }
                }
            }
        }
        Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
    }
}

@Composable
private fun Esp32Row(ip: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("📶", fontSize = 16.sp, color = Muted)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("ESP32 アクセスポイント", color = OnBackground, fontSize = 14.sp)
            if (ip != null) {
                Text(ip, color = Muted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            } else {
                Text("未設定", color = Color(0xFFABABAB), fontSize = 12.sp)
            }
        }
        Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
    }
}

@Composable
private fun DataSection(onExport: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Export Data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⬇", fontSize = 16.sp, color = Primary)
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(0.dp, Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("データをエクスポート", color = OnBackground, fontSize = 14.sp)
                }
                Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
            }
            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            // Cloud Sync
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("☁", fontSize = 16.sp, color = Color(0xFFC0C0C0))
                Spacer(modifier = Modifier.width(12.dp))
                Text("クラウド同期", color = OnBackground, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFC0C0C0)),
                ) {
                    Text(
                        "近日公開",
                        color = Color(0xFFABABAB),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSection(onLicensesClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("バージョン", color = OnBackground, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("1.0.0", color = Muted, fontSize = 14.sp)
            }
            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onLicensesClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(0.dp, Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("オープンソースライセンス", color = Primary, fontSize = 14.sp)
                }
                Text("›", color = Primary, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun StatusDot(connected: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (connected) Secondary else Color(0xFFC0C0C0),
        modifier = Modifier.size(8.dp),
    ) {}
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

private data class LibInfo(val name: String, val version: String, val license: String)

private val libraries = listOf(
    LibInfo("Jetpack Compose", "2024.x", "Apache 2.0"),
    LibInfo("Kotlin", "2.0.0", "Apache 2.0"),
    LibInfo("Room", "2.6.0", "Apache 2.0"),
    LibInfo("Hilt", "2.51.0", "Apache 2.0"),
    LibInfo("Navigation Compose", "2.8.0", "Apache 2.0"),
    LibInfo("Coroutines", "1.8.0", "Apache 2.0"),
)

@Composable
private fun LicensesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "戻る",
                    tint = OnBackground,
                )
            }
            Text("オープンソースライセンス", color = OnBackground, fontSize = 16.sp)
        }
        HorizontalDivider(color = Divider)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(libraries.size) { idx ->
                val lib = libraries[idx]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(lib.name, color = OnBackground, fontSize = 14.sp)
                        Text("v${lib.version}", color = Muted, fontSize = 12.sp)
                    }
                    Text("${lib.license} ライセンス", color = Color(0xFFABABAB), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}
