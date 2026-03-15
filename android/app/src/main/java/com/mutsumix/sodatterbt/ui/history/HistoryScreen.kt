package com.mutsumix.sodatterbt.ui.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
private val Surface2 = Color(0xFFF7F7F7)

private data class HarvestRecord(
    val id: String,
    val variety: String,
    val manufacturer: String,
    val device: String,
    val seedingDate: String,
    val harvestDate: String,
    val days: Int,
    val weight: Int,
)

private val mockRecords = listOf(
    HarvestRecord("1", "Sunny Lettuce", "Takii Seed", "A", "2026/01/04", "2026/02/15", 42, 185),
    HarvestRecord("2", "Mini Tomato", "Sakata Seed", "B", "2026/01/10", "2026/02/20", 41, 142),
    HarvestRecord("3", "Basil", "Tokita Seed", "C", "2026/01/18", "2026/02/28", 41, 98),
    HarvestRecord("4", "Spinach", "Takii Seed", "D", "2025/12/20", "2026/01/28", 39, 210),
    HarvestRecord("5", "Radish", "Sakata Seed", "A", "2025/12/05", "2026/01/12", 38, 165),
)

private fun groupByMonth(records: List<HarvestRecord>): Map<String, List<HarvestRecord>> {
    val groups = mutableMapOf<String, MutableList<HarvestRecord>>()
    records.forEach { r ->
        val parts = r.harvestDate.split("/")
        val key = "${parts[0]}/${parts[1]}"
        groups.getOrPut(key) { mutableListOf() }.add(r)
    }
    return groups
}

private fun formatMonthLabel(key: String): String {
    val parts = key.split("/")
    return "${parts[0]}年${parts[1]}月"
}

private fun formatDateRange(seeding: String, harvest: String, days: Int): String {
    val s = seeding.substring(5)
    val h = harvest.substring(5)
    return "$s → $h（${days}日間）"
}

@Composable
fun HistoryScreen(innerPadding: PaddingValues) {
    var filterOpen by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf<String?>(null) }

    val filtered = if (activeFilter != null) {
        mockRecords.filter { it.device == activeFilter }
    } else {
        mockRecords
    }
    val grouped = groupByMonth(filtered)
    val sortedMonths = grouped.keys.sortedDescending()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Filter bar
        FilterBar(
            filterOpen = filterOpen,
            activeFilter = activeFilter,
            onToggleFilter = { filterOpen = !filterOpen },
            onSelectFilter = { d -> activeFilter = if (activeFilter == d) null else d },
            onClearFilter = { activeFilter = null },
        )

        if (sortedMonths.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                sortedMonths.forEach { month ->
                    item {
                        MonthHeader(month = month)
                    }
                    val records = grouped[month] ?: emptyList()
                    items(records) { record ->
                        Spacer(modifier = Modifier.height(12.dp))
                        HarvestRecordCard(record = record)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatisticsTeaser()
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    filterOpen: Boolean,
    activeFilter: String?,
    onToggleFilter: () -> Unit,
    onSelectFilter: (String) -> Unit,
    onClearFilter: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            OutlinedButton(
                onClick = onToggleFilter,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Divider),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text("フィルター", color = Muted, fontSize = 14.sp)
            }
        }
        if (filterOpen) {
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("デバイス：", color = Muted, fontSize = 12.sp)
                listOf("A", "B", "C", "D").forEach { d ->
                    FilterChip(
                        label = d,
                        selected = activeFilter == d,
                        onClick = { onSelectFilter(d) },
                    )
                }
                if (activeFilter != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onClearFilter,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.dp, Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text("クリア", color = Primary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (selected) Secondary else Divider),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            color = if (selected) Secondary else Muted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun MonthHeader(month: String) {
    Text(
        text = formatMonthLabel(month),
        color = Muted,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun HarvestRecordCard(record: HarvestRecord) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left green accent bar
            Surface(
                color = Secondary,
                modifier = Modifier
                    .width(3.dp)
                    .height(72.dp),
            ) {}

            // Thumbnail placeholder
            Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Surface2,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🌱", fontSize = 20.sp)
                    }
                }
            }

            // Center info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    record.variety,
                    color = OnBackground,
                    fontSize = 16.sp,
                    maxLines = 1,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DeviceSlotBadge(label = record.device, size = 20)
                    Text(
                        text = formatDateRange(record.seedingDate, record.harvestDate, record.days),
                        color = Muted,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }

            // Right weight
            Text(
                text = "${record.weight}g",
                color = OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
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
private fun StatisticsTeaser() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Surface2,
        border = BorderStroke(1.dp, Color(0xFFC8C8C8)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("📊", fontSize = 24.sp, color = Color(0xFFABABAB))
            Text("統計 — 近日公開", color = Color(0xFFABABAB), fontSize = 14.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("📭", fontSize = 40.sp)
            Text("収穫記録がありません。", color = Muted, fontSize = 14.sp)
        }
    }
}
