package com.mutsumix.sodatterbt.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val Surface2 = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var licensesOpen by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf(false) }

    // Edit dialogs state
    var editingKey by remember { mutableStateOf<String?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var editingLabel by remember { mutableStateOf("") }
    var editingDeviceId by remember { mutableStateOf<Int?>(null) }
    var editingError by remember { mutableStateOf<String?>(null) }
    // タグ選択ダイアログ
    var tagPickerDeviceId by remember { mutableStateOf<Int?>(null) }

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
                    DevicesSection(
                        devices = uiState.devices,
                        onTagEdit = { deviceId, _ ->
                            tagPickerDeviceId = deviceId
                            viewModel.fetchAvailableTags()
                        },
                    )
                }
            }
            item {
                SettingsSection(title = "電子ペーパー") {
                    Esp32Section(
                        esp32Ip = uiState.esp32Ip,
                        onEditEsp32 = {
                            editingDeviceId = null
                            editingKey = "esp32_ip"
                            editingValue = uiState.esp32Ip
                            editingLabel = "ESP32 IPアドレス"
                        },
                    )
                }
            }
            item {
                SettingsSection(title = "データ") {
                    DataSection(onExport = { showExportToast = true })
                }
            }
            item {
                SettingsSection(title = "このアプリについて") {
                    AboutSection(onLicensesClick = { licensesOpen = true })
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
                    shape = RoundedCornerShape(24.dp),
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

    // 編集ダイアログ
    if (editingKey != null) {
        Dialog(
            onDismissRequest = { editingKey = null },
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(editingLabel, color = OnBackground, fontSize = 16.sp)
                    OutlinedTextField(
                        value = editingValue,
                        onValueChange = { editingValue = it; editingError = null },
                        singleLine = true,
                        isError = editingError != null,
                        supportingText = editingError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { editingKey = null }) {
                            Text("キャンセル", color = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val devId = editingDeviceId
                            when {
                                devId != null -> {
                                    viewModel.updateTagMac(devId, editingValue.ifBlank { null })
                                    editingKey = null
                                }
                                editingKey == "esp32_ip" -> {
                                    if (viewModel.saveEsp32Ip(editingValue)) {
                                        editingKey = null
                                    } else {
                                        editingError = "IPアドレスの形式が正しくありません（例: 192.168.1.99）"
                                    }
                                }
                            }
                        }) {
                            Text("保存", color = Primary)
                        }
                    }
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

    // タグ選択ダイアログ
    if (tagPickerDeviceId != null) {
        Dialog(onDismissRequest = { tagPickerDeviceId = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("タグを選択", color = OnBackground, fontSize = 16.sp)

                    when {
                        uiState.isFetchingTags -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Primary)
                            }
                        }
                        uiState.tagFetchError != null -> {
                            Text(uiState.tagFetchError!!, color = Color(0xFFEC0000), fontSize = 14.sp)
                        }
                        else -> {
                            // 既に他デバイスに割り当て済みのMACを除外
                            val assignedMacs = uiState.devices
                                .filter { it.id != tagPickerDeviceId }
                                .mapNotNull { it.tagMacAddress }
                                .toSet()
                            val selectableTags = uiState.availableTags.filter { it.mac !in assignedMacs }

                            if (selectableTags.isEmpty()) {
                                Text("選択可能なタグがありません", color = Muted, fontSize = 14.sp)
                            } else {
                                selectableTags.forEach { tag ->
                                    val displayName = tag.alias.ifBlank { tag.mac }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Divider),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateTagMac(tagPickerDeviceId!!, tag.mac)
                                                tagPickerDeviceId = null
                                            },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(displayName, color = OnBackground, fontSize = 14.sp)
                                                if (tag.alias.isNotBlank()) {
                                                    Text(
                                                        tag.mac,
                                                        color = Muted,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                    )
                                                }
                                            }
                                            Text(
                                                "${tag.rssi}dBm",
                                                color = Muted,
                                                fontSize = 11.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = {
                            viewModel.updateTagMac(tagPickerDeviceId!!, null)
                            tagPickerDeviceId = null
                        }) {
                            Text("割り当て解除", color = Color(0xFFEC0000), fontSize = 14.sp)
                        }
                        TextButton(onClick = { tagPickerDeviceId = null }) {
                            Text("キャンセル", color = Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Muted,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        content()
    }
}

@Composable
private fun DevicesSection(
    devices: List<DeviceEntity>,
    onTagEdit: (deviceId: Int, currentMac: String?) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            devices.forEachIndexed { idx, device ->
                DeviceRow(device = device, onEdit = { onTagEdit(device.id, device.tagMacAddress) })
                if (idx < devices.size - 1) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceEntity, onEdit: () -> Unit) {
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
            DeviceSlotBadge(label = device.name)
            Column {
                Text("デバイス ${device.name}", color = OnBackground, fontSize = 14.sp)
                if (device.tagMacAddress != null) {
                    Text(
                        "タグ: ${device.tagMacAddress}",
                        color = Muted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                } else {
                    Text("未割り当て", color = Color(0xFFABABAB), fontSize = 12.sp)
                }
            }
        }
        TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
            Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
        }
    }
}

@Composable
private fun Esp32Section(
    esp32Ip: String,
    onEditEsp32: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SettingRow(
            icon = "📶",
            label = "ESP32 アクセスポイント",
            value = esp32Ip.ifBlank { "未設定" },
            connected = esp32Ip.isNotBlank(),
            onEdit = onEditEsp32,
        )
    }
}

@Composable
private fun SettingRow(
    icon: String,
    label: String,
    value: String,
    connected: Boolean,
    onEdit: () -> Unit,
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
            Text(label, color = OnBackground, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusDot(connected = connected)
                Text(
                    value,
                    color = if (connected) Muted else Color(0xFFABABAB),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
            Text("›", color = Color(0xFFC0C0C0), fontSize = 18.sp)
        }
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
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
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
            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 16.dp))
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
    LibInfo("Jetpack Compose", "2024.12.01", "Apache 2.0"),
    LibInfo("Kotlin", "2.0.21", "Apache 2.0"),
    LibInfo("Room", "2.6.1", "Apache 2.0"),
    LibInfo("Hilt", "2.52", "Apache 2.0"),
    LibInfo("Navigation Compose", "2.8.5", "Apache 2.0"),
    LibInfo("Coroutines", "1.9.0", "Apache 2.0"),
    LibInfo("OkHttp", "4.12.0", "Apache 2.0"),
    LibInfo("Coil", "2.7.0", "Apache 2.0"),
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
