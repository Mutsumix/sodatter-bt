package com.mutsumix.sodatterbt.ui.seeding

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
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
private val Error = Color(0xFFEC0000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedingScreen(
    deviceId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SeedingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showNoTagConfirm by remember { mutableStateOf(false) }

    // deviceIdが有効な場合は初期選択
    LaunchedEffect(deviceId, uiState.deviceOptions) {
        if (deviceId > 0 && uiState.selectedDeviceId == null) {
            val option = uiState.deviceOptions.firstOrNull { it.id == deviceId && !it.inUse }
            if (option != null) viewModel.selectDevice(option.id)
        }
    }

    // 保存完了ダイアログ（ユーザーがOKを押すまで遷移しない）

    // 電子タグ連携なし確認ダイアログ
    if (showNoTagConfirm) {
        Dialog(onDismissRequest = { showNoTagConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("電子タグとの連携なしで登録しますか？", color = OnBackground, fontSize = 16.sp)
                    Text(
                        "ESP32 IPアドレスまたはタグMACアドレスが未設定のため、電子ペーパータグは更新されません。",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showNoTagConfirm = false }) {
                            Text("キャンセル", color = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showNoTagConfirm = false
                            viewModel.register()
                        }) {
                            Text("登録する", color = Primary)
                        }
                    }
                }
            }
        }
    }

    if (uiState.isSaved && !uiState.isTagUpdating) {
        val tagMsg = uiState.tagUpdateMessage
        val isTagError = tagMsg != null && tagMsg.contains("失敗")
        Dialog(onDismissRequest = { }) {
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
                        border = BorderStroke(1.dp, if (isTagError) Error else Secondary),
                        modifier = Modifier.size(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (isTagError) "!" else "✓", color = if (isTagError) Error else Secondary, fontSize = 24.sp)
                        }
                    }
                    Text("登録が完了しました", color = OnBackground, fontSize = 16.sp)
                    Text(
                        "デバイス ${uiState.savedDeviceName} に播種情報を登録しました",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    if (tagMsg != null) {
                        Text(
                            tagMsg,
                            color = if (isTagError) Error else Secondary,
                            fontSize = 13.sp,
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.clearTagUpdateMessage()
                            onSaved()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Primary),
                    ) {
                        Text("OK", color = Primary, fontSize = 16.sp)
                    }
                }
            }
        }
    } else if (uiState.isSaved && uiState.isTagUpdating) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("電子ペーパータグ更新中…", color = Muted, fontSize = 14.sp)
                }
            }
        }
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
            SeedingBottomBar(onRegister = {
                scope.launch {
                    if (viewModel.hasTagLink()) {
                        viewModel.register()
                    } else {
                        showNoTagConfirm = true
                    }
                }
            })
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
                devices = uiState.deviceOptions,
                selectedDeviceId = uiState.selectedDeviceId,
                errorMessage = uiState.deviceError,
                onDeviceSelected = { viewModel.selectDevice(it) },
            )
            VarietyInputSection(
                value = uiState.variety,
                errorMessage = uiState.varietyError,
                onValueChange = { viewModel.setVariety(it) },
            )
            ManufacturerInputSection(
                value = uiState.manufacturer,
                onValueChange = { viewModel.setManufacturer(it) },
            )
            SeedingDateSection(millis = uiState.seedingDateMillis)
            SeedPhotoSection(
                photoUri = uiState.seedPhotoUri,
                onPhotoTaken = { uri -> viewModel.setSeedPhotoUri(uri.toString()) },
                onRemove = { viewModel.setSeedPhotoUri(null) },
            )
        }
    }
}

@Composable
private fun DeviceSelectorSection(
    devices: List<DeviceOption>,
    selectedDeviceId: Int?,
    errorMessage: String,
    onDeviceSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Device")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            devices.forEach { device ->
                DeviceOptionButton(
                    device = device,
                    isSelected = selectedDeviceId == device.id,
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
    val borderColor = if (device.inUse) Divider else Secondary
    val textColor = if (device.inUse) Color(0xFFB0B0B0) else Secondary
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
            Text(device.name, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
private fun SeedingDateSection(millis: Long) {
    val display = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN).format(Date(millis))
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
                Text(display, color = OnBackground, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SeedPhotoSection(
    photoUri: String?,
    onPhotoTaken: (Uri) -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCamera by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) showCamera = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "種の写真")
        if (photoUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCamera = true },
                )
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text("削除", color = Color(0xFFEC0000), fontSize = 12.sp)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Divider),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable {
                        if (hasCameraPermission) showCamera = true
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("タップして種袋や播種時の様子を撮影（任意）", color = Muted, fontSize = 14.sp)
                }
            }
        }
    }

    if (showCamera) {
        SeedCameraDialog(
            onCaptured = { uri ->
                showCamera = false
                onPhotoTaken(uri)
            },
            onDismiss = { showCamera = false },
        )
    }
}

@Composable
private fun SeedCameraDialog(
    onCaptured: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル", color = Color.White, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = {
                        val file = java.io.File(
                            context.filesDir,
                            "photos/seed_${System.currentTimeMillis()}.jpg"
                        )
                        file.parentFile?.mkdirs()
                        val options = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onCaptured(Uri.fromFile(file))
                                }
                                override fun onError(exc: ImageCaptureException) {}
                            }
                        )
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    border = BorderStroke(2.dp, Secondary),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(52.dp),
                    ) {}
                }
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
