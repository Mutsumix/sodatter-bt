package com.mutsumix.sodatterbt.ui.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mutsumix.sodatterbt.data.repository.DeviceWithCultivation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onDeviceClick: (deviceId: Int) -> Unit,
    onEmptySlotClick: (deviceId: Int) -> Unit,
    onQrScanClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(uiState.devices) { item ->
            if (item.cultivation == null) {
                EmptyDeviceCard(
                    deviceLabel = item.device.name,
                    onClick = { onEmptySlotClick(item.device.id) },
                )
            } else {
                ActiveDeviceCard(
                    item = item,
                    onClick = { onDeviceClick(item.device.id) },
                )
            }
        }
        item(span = { GridItemSpan(2) }) {
            OutlinedButton(
                onClick = onQrScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Primary),
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("タグをスキャン", color = Primary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EmptyDeviceCard(
    deviceLabel: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Divider),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DeviceSlotBadge(label = deviceLabel)
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "タップして登録",
                color = Muted,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ActiveDeviceCard(
    item: DeviceWithCultivation,
    onClick: () -> Unit,
) {
    val cultivation = item.cultivation!!
    val daysElapsed = ((System.currentTimeMillis() - cultivation.seedingDate) / 86_400_000L).toInt()
    val seedingDateStr = dateFormat.format(Date(cultivation.seedingDate))

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Secondary),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                DeviceSlotBadge(label = item.device.name)
                if (cultivation.seedPhotoUri != null) {
                    AsyncImage(
                        model = cultivation.seedPhotoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = cultivation.varietyName,
                color = OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "播種日: $seedingDateStr",
                color = Muted,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DayBadge(days = daysElapsed)
        }
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
            Text(
                text = label,
                color = Secondary,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun DayBadge(days: Int) {
    Text(
        text = "Day $days",
        color = Primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}
