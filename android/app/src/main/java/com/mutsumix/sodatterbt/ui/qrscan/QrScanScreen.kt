package com.mutsumix.sodatterbt.ui.qrscan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)

@Composable
fun QrScanScreen(
    cultivationId: Long,
    onNavigateToPhotoRecord: (deviceId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: QrScanViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    var notFound by remember { mutableStateOf(false) }

    if (cultivationId >= 0L) {
        // ディープリンク経由: cultivationIdからdeviceIdを解決して遷移
        LaunchedEffect(cultivationId) {
            val deviceId = viewModel.resolveDeviceId(cultivationId)
            if (deviceId != null) {
                onNavigateToPhotoRecord(deviceId)
            } else {
                notFound = true
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            if (notFound) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("栽培記録が見つかりませんでした", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onBack) {
                        Text("戻る", color = Primary, fontSize = 16.sp)
                    }
                }
            } else {
                Text("読み込み中…", color = Color.White, fontSize = 16.sp)
            }
        }
    } else {
        // カメラでQR手動スキャン
        QrScanningScreen(
            onQrDetected = { rawValue ->
                // sodatterbt://cultivation/{id} をパース
                val uri = Uri.parse(rawValue)
                if (uri.scheme == "sodatterbt" && uri.host == "cultivation") {
                    val id = uri.lastPathSegment?.toLongOrNull()
                    if (id != null) {
                        scope.launch {
                            val deviceId = viewModel.resolveDeviceId(id)
                            if (deviceId != null) {
                                onNavigateToPhotoRecord(deviceId)
                            }
                        }
                    }
                }
            },
            onBack = onBack,
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun QrScanningScreen(
    onQrDetected: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var detected by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (hasCameraPermission) {
            val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
            val barcodeScanner = remember { BarcodeScanning.getClient() }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !detected) {
                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            if (barcode.format == Barcode.FORMAT_QR_CODE && barcode.rawValue != null) {
                                                detected = true
                                                onQrDetected(barcode.rawValue!!)
                                                break
                                            }
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis,
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // UI オーバーレイ
        TopScanOverlay()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ScanFrame(isDetected = detected)
        }

        BottomScanOverlay(
            isScanning = !detected,
            onCancel = onBack,
        )
    }
}

@Composable
private fun TopScanOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.72f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 400f,
                    ),
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = "容器のタグをスキャンしてください",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 60.dp, start = 32.dp, end = 32.dp),
            )
        }
    }
}

@Composable
private fun ScanFrame(isDetected: Boolean) {
    val frameColor = if (isDetected) Secondary else Primary
    val cornerLength = 28.dp

    Box(
        modifier = Modifier
            .size(220.dp)
            .drawWithContent {
                drawContent()
                val stroke = Stroke(width = 2.dp.toPx())
                val cl = cornerLength.toPx()
                val w = size.width
                val h = size.height

                drawLine(frameColor, Offset(0f, cl), Offset(0f, 0f), stroke.width)
                drawLine(frameColor, Offset(0f, 0f), Offset(cl, 0f), stroke.width)
                drawLine(frameColor, Offset(w - cl, 0f), Offset(w, 0f), stroke.width)
                drawLine(frameColor, Offset(w, 0f), Offset(w, cl), stroke.width)
                drawLine(frameColor, Offset(0f, h - cl), Offset(0f, h), stroke.width)
                drawLine(frameColor, Offset(0f, h), Offset(cl, h), stroke.width)
                drawLine(frameColor, Offset(w - cl, h), Offset(w, h), stroke.width)
                drawLine(frameColor, Offset(w, h - cl), Offset(w, h), stroke.width)

                if (isDetected) {
                    drawRoundRect(
                        color = Secondary.copy(alpha = 0.08f),
                        size = size,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawRoundRect(
                        color = Secondary,
                        size = size,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            },
    ) {}
}

@Composable
private fun BottomScanOverlay(
    isScanning: Boolean,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        startY = 400f,
                    ),
                )
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (isScanning) {
                TextButton(onClick = onCancel) {
                    Text("キャンセル", color = Color.White, fontSize = 16.sp)
                }
            } else {
                Text("QRコードを検出しました…", color = Color.White, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
