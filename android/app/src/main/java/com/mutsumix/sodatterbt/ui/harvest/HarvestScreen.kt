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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val cultivation = uiState.cultivation
    val device = uiState.device

    // ステップ式収穫確認ダイアログ
    // step: 0=非表示, 1=重量未取得警告, 2=ラベル印刷確認, 3=最終確認
    var harvestStep by remember { mutableIntStateOf(0) }

    fun startHarvestFlow() {
        if (uiState.weightGram <= 0f) {
            harvestStep = 1 // 重量未取得
        } else if (uiState.printerEnabled && !uiState.hasPrinted) {
            harvestStep = 2 // ラベル未印刷
        } else {
            harvestStep = 3 // 最終確認
        }
    }

    if (harvestStep > 0) {
        HarvestConfirmDialog(
            step = harvestStep,
            onDismiss = { harvestStep = 0 },
            onSkipWeight = {
                if (uiState.printerEnabled && !uiState.hasPrinted) harvestStep = 2 else harvestStep = 3
            },
            onPrintLabel = {
                harvestStep = 0
                onLabelPrintClick(uiState.weightGram)
            },
            onSkipLabel = { harvestStep = 3 },
            onConfirm = {
                harvestStep = 0
                viewModel.complete()
            },
        )
    }

    // 収穫完了ダイアログ（OKを押すまで遷移しない）
    if (uiState.isCompleted) {
        HarvestCompleteDialog(
            deviceName = uiState.completedDeviceName,
            cropName = uiState.completedCropName,
            weightGram = uiState.weightGram,
            onOk = onComplete,
        )
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
                onComplete = { startHarvestFlow() },
                onLabelPrint = {
                    viewModel.markPrinted()
                    onLabelPrintClick(uiState.weightGram)
                },
                showPrintButton = uiState.printerEnabled,
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
                onManualInput = { viewModel.setManualWeight(it) },
                showScaleConnect = uiState.scaleEnabled,
            )
            HarvestDateField(millis = System.currentTimeMillis())
        }
    }
}

@Composable
private fun HarvestConfirmDialog(
    step: Int,
    onDismiss: () -> Unit,
    onSkipWeight: () -> Unit,
    onPrintLabel: () -> Unit,
    onSkipLabel: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 進捗表示
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (i in 1..3) {
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = if (i <= step) Primary else Divider,
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                        ) {}
                    }
                }

                when (step) {
                    1 -> {
                        Text("重量が取得されていません", color = OnBackground, fontSize = 16.sp)
                        Text(
                            "スケールで重量を計測せずに収穫を完了しますか？",
                            color = Muted,
                            fontSize = 14.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("戻る", color = Muted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onSkipWeight) {
                                Text("重量なしで続行", color = Primary)
                            }
                        }
                    }
                    2 -> {
                        Text("ラベルを印刷しますか？", color = OnBackground, fontSize = 16.sp)
                        Text(
                            "収穫ラベルをまだ印刷していません。",
                            color = Muted,
                            fontSize = 14.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = onSkipLabel) {
                                Text("印刷せず続行", color = Muted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onPrintLabel) {
                                Text("ラベルを印刷", color = Primary)
                            }
                        }
                    }
                    3 -> {
                        Text("収穫を完了しますか？", color = OnBackground, fontSize = 16.sp)
                        Text(
                            "この操作は取り消せません。栽培記録が収穫済みになります。",
                            color = Muted,
                            fontSize = 14.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("キャンセル", color = Muted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onConfirm) {
                                Text("収穫を完了", color = Secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HarvestCompleteDialog(
    deviceName: String,
    cropName: String,
    weightGram: Float,
    onOk: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Secondary),
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✂", color = Secondary, fontSize = 24.sp)
                    }
                }
                Text("収穫が完了しました", color = OnBackground, fontSize = 16.sp)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("デバイス $deviceName（$cropName）", color = Muted, fontSize = 14.sp)
                    if (weightGram > 0f) {
                        Text("収穫量：${weightGram}g", color = Muted, fontSize = 14.sp)
                    }
                    Text(
                        "収穫日：${dateFormat.format(Date(System.currentTimeMillis()))}",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                }
                OutlinedButton(
                    onClick = onOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Primary),
                ) {
                    Text("OK", color = Primary, fontSize = 16.sp)
                }
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
    onManualInput: (Float) -> Unit,
    showScaleConnect: Boolean = true,
) {
    var showManualDialog by remember { mutableStateOf(false) }

    if (showManualDialog) {
        ManualWeightDialog(
            currentWeight = weightGram,
            onDismiss = { showManualDialog = false },
            onConfirm = { weight ->
                onManualInput(weight)
                showManualDialog = false
            },
        )
    }

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
            else -> Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (showScaleConnect) {
                    OutlinedButton(
                        onClick = onConnect,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Primary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text("スケールを接続", color = Primary, fontSize = 14.sp)
                    }
                }
                OutlinedButton(
                    onClick = { showManualDialog = true },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (showScaleConnect) Muted else Primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text("重量を手入力", color = if (showScaleConnect) Muted else Primary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ManualWeightDialog(
    currentWeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    var text by remember {
        mutableStateOf(if (currentWeight > 0f) currentWeight.toString() else "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("重量を入力", color = OnBackground, fontSize = 16.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; error = null },
                    singleLine = true,
                    suffix = { Text("g", color = Muted) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル", color = Muted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val parsed = text.toFloatOrNull()
                        if (parsed == null || parsed < 0f) {
                            error = "正の数値を入力してください"
                        } else {
                            onConfirm(parsed)
                        }
                    }) {
                        Text("確定", color = Primary)
                    }
                }
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
    showPrintButton: Boolean = true,
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = Divider)
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                ) {
                    Text("✂", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("収穫を完了する", color = Color.White, fontSize = 16.sp)
                }
                if (showPrintButton) {
                Button(
                    onClick = onLabelPrint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text("ラベルを印刷", color = Color.White, fontSize = 16.sp)
                }
                }
            }
        }
    }
}
