package com.mutsumix.sodatterbt.ui.seeding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val Error = Color(0xFFEC0000)

private data class DeviceOption(val id: String, val inUse: Boolean)

private val deviceOptions = listOf(
    DeviceOption("A", inUse = true),
    DeviceOption("B", inUse = true),
    DeviceOption("C", inUse = false),
    DeviceOption("D", inUse = false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedingScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var variety by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var seedingDate by remember { mutableStateOf("2026-03-15") }
    var deviceError by remember { mutableStateOf("") }
    var varietyError by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        SeedingSuccessScreen(
            deviceId = selectedDevice ?: "",
            onBack = onBack,
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Seeding", color = OnBackground, fontSize = 16.sp) },
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
            SeedingBottomBar(
                onRegister = {
                    var hasError = false
                    if (selectedDevice == null) {
                        deviceError = "デバイスを選択してください"
                        hasError = true
                    }
                    if (variety.isBlank()) {
                        varietyError = "品種名を入力してください"
                        hasError = true
                    }
                    if (!hasError) {
                        submitted = true
                        onSaved()
                    }
                },
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
            DeviceSelectorSection(
                devices = deviceOptions,
                selectedDevice = selectedDevice,
                errorMessage = deviceError,
                onDeviceSelected = { id ->
                    selectedDevice = id
                    deviceError = ""
                },
            )
            VarietyInputSection(
                value = variety,
                errorMessage = varietyError,
                onValueChange = { variety = it; varietyError = "" },
            )
            ManufacturerInputSection(
                value = manufacturer,
                onValueChange = { manufacturer = it },
            )
            SeedingDateSection(
                value = seedingDate,
            )
            SeedPhotoSection()
        }
    }
}

@Composable
private fun SeedingSuccessScreen(
    deviceId: String,
    onBack: () -> Unit,
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
                    Text("✓", color = Secondary, fontSize = 28.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("登録が完了しました", color = OnBackground, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "デバイス $deviceId に播種情報を登録しました",
                    color = Muted,
                    fontSize = 14.sp,
                )
            }
            OutlinedButton(
                onClick = onBack,
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
private fun DeviceSelectorSection(
    devices: List<DeviceOption>,
    selectedDevice: String?,
    errorMessage: String,
    onDeviceSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Device")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            devices.forEach { device ->
                DeviceOptionButton(
                    device = device,
                    isSelected = selectedDevice == device.id,
                    onClick = { if (!device.inUse) onDeviceSelected(device.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (errorMessage.isNotEmpty()) {
            ErrorText(message = errorMessage)
        }
    }
}

@Composable
private fun DeviceOptionButton(
    device: DeviceOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        device.inUse -> Divider
        isSelected -> Secondary
        else -> Secondary
    }
    val textColor = when {
        device.inUse -> Color(0xFFB0B0B0)
        isSelected -> Secondary
        else -> Secondary
    }
    val borderWidth = if (isSelected && !device.inUse) 2.dp else 1.dp

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(borderWidth, borderColor),
        enabled = !device.inUse,
        contentPadding = PaddingValues(4.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(device.id, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (device.inUse) {
                Text("In use", color = Color(0xFFB0B0B0), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun VarietyInputSection(
    value: String,
    errorMessage: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Variety")
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：ミニトマト", color = Muted) },
            isError = errorMessage.isNotEmpty(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                errorBorderColor = Error,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground,
            ),
            singleLine = true,
        )
        if (errorMessage.isNotEmpty()) {
            ErrorText(message = errorMessage)
        }
    }
}

@Composable
private fun ManufacturerInputSection(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Manufacturer")
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：タキイ種苗", color = Muted) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground,
            ),
            singleLine = true,
        )
    }
}

@Composable
private fun SeedingDateSection(value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Seeding Date")
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val parts = value.split("-")
                val display = if (parts.size == 3) "${parts[0]}年${parts[1]}月${parts[2]}日" else value
                Text(display, color = OnBackground, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SeedPhotoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Seed Photo")
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("📷", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Take a photo (optional)", color = Muted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SeedingBottomBar(onRegister: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = Divider)
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Secondary),
                ) {
                    Text("Register", color = Secondary, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, color = OnBackground, fontSize = 14.sp)
}

@Composable
private fun ErrorText(message: String) {
    Text(text = message, color = Error, fontSize = 12.sp)
}
