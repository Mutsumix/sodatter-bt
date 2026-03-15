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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

private data class DeviceItem(
    val id: String,
    val cropName: String = "",
    val seedingDate: String = "",
    val daysElapsed: Int = 0,
    val isEmpty: Boolean = false,
)

private val mockDevices = listOf(
    DeviceItem(id = "A", cropName = "ミニトマト", seedingDate = "2024-01-15", daysElapsed = 32, isEmpty = false),
    DeviceItem(id = "B", cropName = "バジル", seedingDate = "2024-02-01", daysElapsed = 15, isEmpty = false),
    DeviceItem(id = "C", isEmpty = true),
    DeviceItem(id = "D", isEmpty = true),
)

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onDeviceClick: (deviceId: Int) -> Unit,
    onEmptySlotClick: (deviceId: Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(mockDevices) { device ->
            if (device.isEmpty) {
                EmptyDeviceCard(
                    deviceId = device.id,
                    onClick = { onEmptySlotClick(device.id.first().code) },
                )
            } else {
                ActiveDeviceCard(
                    device = device,
                    onClick = { onDeviceClick(device.id.first().code) },
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceCard(
    deviceId: String,
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
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    device: DeviceItem,
    onClick: () -> Unit,
) {
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
                DeviceSlotBadge(label = device.id)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = device.cropName,
                color = OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "播種日: ${device.seedingDate}",
                color = Muted,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DayBadge(days = device.daysElapsed)
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
