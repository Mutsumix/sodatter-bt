package com.mutsumix.sodatterbt.ui.labelprint

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
import androidx.compose.foundation.layout.wrapContentWidth
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
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
fun LabelPrintScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: LabelPrintViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val btPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            viewModel.connectPrinter()
        }
    }

    fun connectPrinterWithPermission() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.connectPrinter()
        } else {
            btPermissionLauncher.launch(permissions)
        }
    }

    // トーストを一定時間後にクリア
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            kotlinx.coroutines.delay(2_500)
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    isPrinting = uiState.isPrinting,
                    onPrint = { viewModel.print() },
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
                val cultivation = uiState.cultivation
                val device = uiState.device
                if (cultivation != null && device != null) {
                    LabelMockup(
                        device = device,
                        cultivation = cultivation,
                        overrideWeightGram = uiState.currentWeightGram,
                    )
                }
                PrinterStatusCard(
                    connected = uiState.printerConnected,
                    isDiscovering = uiState.isDiscovering,
                    onConnect = { connectPrinterWithPermission() },
                )
            }
        }

        if (uiState.toastMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 148.dp)
                    .background(
                        color = OnBackground.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(text = uiState.toastMessage!!, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun LabelMockup(device: DeviceEntity, cultivation: CultivationEntity, overrideWeightGram: Float = 0f) {
    val daysElapsed = cultivation.harvestDate?.let {
        ((it - cultivation.seedingDate) / 86_400_000L).toInt()
    } ?: ((System.currentTimeMillis() - cultivation.seedingDate) / 86_400_000L).toInt()
    val seedingDateStr = dateFormat.format(Date(cultivation.seedingDate))
    val harvestDateStr = dateFormat.format(Date(cultivation.harvestDate ?: System.currentTimeMillis()))
    val effectiveWeight = cultivation.harvestWeightGram ?: overrideWeightGram.takeIf { it > 0f }
    val weightStr = effectiveWeight?.let {
        if (it % 1f == 0f) "${it.toInt()}.0" else "$it"
    } ?: "---"

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
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(cultivation.varietyName, color = OnBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(cultivation.manufacturer, color = Muted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Divider)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("播種: $seedingDateStr", color = OnBackground, fontSize = 14.sp)
                    Text("収穫: $harvestDateStr", color = OnBackground, fontSize = 14.sp)
                    Text("重量: ${weightStr}g", color = OnBackground, fontSize = 14.sp)
                    Text("装置${device.name}  Day $daysElapsed", color = Muted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Divider)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        QrCodeMock(size = 80)
                    }
                }
                PerforatedEdge()
            }
        }
        Text("QRコードは生育フォトログにリンクします", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun PerforatedEdge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(20) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Divider),
            )
        }
    }
}

@Composable
private fun QrCodeMock(size: Int) {
    val cells = listOf(
        listOf(1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1),
        listOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1),
        listOf(1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1),
        listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        listOf(1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1),
        listOf(0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0),
        listOf(1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 1),
        listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0),
        listOf(1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0),
        listOf(1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1),
        listOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0),
        listOf(1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1),
    )
    val cellSizeDp = (size / cells.size).dp
    Column(modifier = Modifier.size(size.dp)) {
        cells.forEach { row ->
            Row {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .size(cellSizeDp)
                            .background(if (cell == 1) OnBackground else Color.White),
                    )
                }
            }
        }
    }
}

@Composable
private fun PrinterStatusCard(
    connected: Boolean,
    isDiscovering: Boolean,
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
                Text("Star SM-S210i", color = OnBackground, fontSize = 14.sp, fontFamily = FontFamily.Default)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatusDot(connected = connected)
                    Text(
                        when {
                            connected -> "接続済み"
                            isDiscovering -> "検索中…"
                            else -> "未接続"
                        },
                        color = if (connected) Muted else Color(0xFFABABAB),
                        fontSize = 12.sp,
                    )
                }
            }
            if (!connected) {
                OutlinedButton(
                    onClick = onConnect,
                    enabled = !isDiscovering,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(if (isDiscovering) "接続中…" else "接続する", color = Primary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusDot(connected: Boolean) {
    Surface(
        shape = CircleShape,
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
    isPrinting: Boolean,
    onPrint: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = Divider)
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = onPrint,
                    enabled = !isPrinting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Primary),
                ) {
                    Text(if (isPrinting) "印刷中…" else "印刷", color = Primary, fontSize = 16.sp)
                }
            }
        }
    }
}
