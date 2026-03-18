package com.mutsumix.sodatterbt.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val Surface2 = Color(0xFFF7F7F7)

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
private val monthDayFormat = SimpleDateFormat("MM/dd", Locale.JAPAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    deviceId: Int,
    promptCamera: Boolean = false,
    onBack: () -> Unit,
    onHarvestClick: () -> Unit,
    onPhotoClick: () -> Unit = {},
    onDeleted: () -> Unit = onBack,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCameraPrompt by remember { mutableStateOf(promptCamera) }
    var expandedPhoto by remember { mutableStateOf<GrowthPhotoEntity?>(null) }
    var showPhotoDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val cultivation = uiState.cultivation
    val device = uiState.device

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = cultivation?.varietyName ?: "",
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
                actions = {
                    if (cultivation != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "削除",
                                tint = Color(0xFFEC0000),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        bottomBar = {
            if (cultivation?.isActive == true) {
                HarvestBottomBar(onHarvestClick = onHarvestClick)
            }
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
            if (cultivation != null && device != null) {
                InfoCard(device = device, cultivation = cultivation)
                GrowthLogSection(
                    photos = uiState.growthPhotos,
                    onPhotoClick = onPhotoClick,
                    onThumbnailClick = { photo -> expandedPhoto = photo },
                )
            }
        }
    }

    // QRスキャン経由のカメラ撮影確認ダイアログ
    if (showCameraPrompt && cultivation != null) {
        Dialog(onDismissRequest = { showCameraPrompt = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("写真を撮影しますか？", color = OnBackground, fontSize = 16.sp)
                    Text(
                        "「${cultivation.varietyName}」の生育記録として写真を追加します。",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showCameraPrompt = false }) {
                            Text("あとで", color = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showCameraPrompt = false
                            onPhotoClick()
                        }) {
                            Text("撮影する", color = Primary)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && cultivation != null) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("栽培記録を削除しますか？", color = OnBackground, fontSize = 16.sp)
                    Text(
                        "「${cultivation.varietyName}」の栽培記録と関連する写真データが削除されます。この操作は取り消せません。",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("キャンセル", color = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteCultivation()
                        }) {
                            Text("削除", color = Color(0xFFEC0000))
                        }
                    }
                }
            }
        }
    }

    // 写真拡大表示
    if (expandedPhoto != null) {
        Dialog(
            onDismissRequest = { expandedPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { expandedPhoto = null },
            ) {
                AsyncImage(
                    model = expandedPhoto!!.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                IconButton(
                    onClick = { showPhotoDeleteConfirm = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "削除",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = dateFormat.format(Date(expandedPhoto!!.takenAt)),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                )
            }
        }
    }

    // 写真削除確認
    if (showPhotoDeleteConfirm && expandedPhoto != null) {
        Dialog(onDismissRequest = { showPhotoDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("この写真を削除しますか？", color = OnBackground, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showPhotoDeleteConfirm = false }) {
                            Text("キャンセル", color = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            viewModel.deletePhoto(expandedPhoto!!.id)
                            showPhotoDeleteConfirm = false
                            expandedPhoto = null
                        }) {
                            Text("削除", color = Color(0xFFEC0000))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(device: DeviceEntity, cultivation: CultivationEntity) {
    val effectiveEnd = cultivation.harvestDate ?: System.currentTimeMillis()
    val daysElapsed = ((effectiveEnd - cultivation.seedingDate) / 86_400_000L).toInt()
    val seedingDateStr = dateFormat.format(Date(cultivation.seedingDate))
    val harvestDateStr = cultivation.harvestDate?.let { dateFormat.format(Date(it)) }
    val weightStr = cultivation.harvestWeightGram?.let {
        if (it % 1f == 0f) "${it.toInt()}g" else "${it}g"
    }

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
                        DeviceSlotBadge(label = device.name)
                        Text(cultivation.varietyName, color = OnBackground, fontSize = 16.sp)
                    }
                    Text(cultivation.manufacturer, color = Muted, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "播種日：$seedingDateStr",
                        color = Muted,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(16.dp)
                            .padding(horizontal = 12.dp),
                        color = Divider,
                    )
                    Text(
                        "Day $daysElapsed",
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (harvestDateStr != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("収穫日：$harvestDateStr", color = Muted, fontSize = 14.sp)
                        if (weightStr != null) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(16.dp)
                                    .padding(horizontal = 12.dp),
                                color = Divider,
                            )
                            Text("収穫量：$weightStr", color = Muted, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthLogSection(
    photos: List<GrowthPhotoEntity>,
    onPhotoClick: () -> Unit = {},
    onThumbnailClick: (GrowthPhotoEntity) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("生育ログ", color = OnBackground, fontSize = 16.sp)
            IconButton(onClick = onPhotoClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "写真を撮る",
                    tint = Primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (photos.isEmpty()) {
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
                        text = "まだ写真がありません。\nカメラボタンから撮影してください。",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                photos.forEach { photo ->
                    GrowthLogThumbnail(
                        photoUri = photo.photoUri,
                        takenAt = photo.takenAt,
                        onClick = { onThumbnailClick(photo) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthLogThumbnail(photoUri: String, takenAt: Long, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        )
        Text(monthDayFormat.format(Date(takenAt)), color = Muted, fontSize = 10.sp)
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
