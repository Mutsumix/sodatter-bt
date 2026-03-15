package com.mutsumix.sodatterbt.ui.harvest

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onLabelPrintClick: (Float) -> Unit,
    onComplete: () -> Unit,
    viewModel: HarvestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val blePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            viewModel.connectScale()
        }
    }

    fun connectScaleWithPermission() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.connectScale()
        } else {
            blePermissionLauncher.launch(permissions)
        }
    }

    // 収穫完了後にナビゲーション
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onComplete()
    }

    val cultivation = uiState.cultivation
    val device = uiState.device

    if (uiState.isCompleted) {
        HarvestCompleteScreen(
            deviceName = device?.name ?: "",
            cropName = cultivation?.varietyName ?: "",
            weightGram = uiState.weightGram,
            harvestDate = System.currentTimeMillis(),
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
                onComplete = { viewModel.complete() },
                onLabelPrint = { onLabelPrintClick(uiState.weightGram) },
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
            if (device != null && cultivation != null) {
                val daysElapsed = ((System.currentTimeMillis() - cultivation.seedingDate) / 86_400_000L).toInt()
                CropInfoCard(
                    deviceName = device.name,
                    cropName = cultivation.varietyName,
                    seedingDate = dateFormat.format(Date(cultivation.seedingDate)),
                    daysElapsed = daysElapsed,
                )
            }
            WeightDisplay(
                weightGram = uiState.weightGram,
                scaleConnected = uiState.scaleConnected,
                isScanning = uiState.isScanning,
                onTare = { viewModel.tare() },
                onConnect = { connectScaleWithPermission() },
            )
            HarvestDateField(millis = System.currentTimeMillis())
        }
    }
}

@Composable
private fun HarvestCompleteScreen(
    deviceName: String,
    cropName: String,
    weightGram: Float,
    harvestDate: Long,
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
                Text("デバイス $deviceName（$cropName）", color = Muted, fontSize = 14.sp)
                if (weightGram > 0f) {
                    Text("収穫量：${weightGram}g", color = Muted, fontSize = 14.sp)
                }
                Text(
                    "収穫日：${dateFormat.format(Date(harvestDate))}",
                    color = Muted,
                    fontSize = 14.sp,
                )
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
private fun CropInfoCard(
    deviceName: String,
    cropName: String,
    seedingDate: String,
    daysElapsed: Int,
) {
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
            DeviceSlotBadge(label = deviceName)
            Spacer(modifier = Modifier.width(12.dp))
            Text(cropName, color = OnBackground, fontSize = 16.sp, modifier = Modifier.weight(1f))
            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 12.dp),
                color = Divider,
            )
            Text("播種日 $seedingDate", color = Muted, fontSize = 14.sp)
            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 12.dp),
                color = Divider,
            )
            Text("Day $daysElapsed", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WeightDisplay(
    weightGram: Float,
    scaleConnected: Boolean,
    isScanning: Boolean,
    onTare: () -> Unit,
    onConnect: () -> Unit,
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
            val weightText = if (weightGram == 0f) "---" else
                if (weightGram % 1f == 0f) "${weightGram.toInt()}.0" else "$weightGram"
            Text(text = weightText, color = OnBackground, fontSize = 48.sp)
            Text(text = "g", color = Muted, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
            if (scaleConnected) {
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
        }
        when {
            scaleConnected -> StatusDot(connected = true, label = "Decent Scale：接続済み")
            isScanning -> StatusDot(connected = false, label = "スキャン中…")
            else -> OutlinedButton(
                onClick = onConnect,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Primary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text("スケールを接続", color = Primary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun HarvestDateField(millis: Long) {
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
                Text(dateFormat.format(Date(millis)), color = OnBackground, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun StatusDot(connected: Boolean, label: String) {
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
            text = label,
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
