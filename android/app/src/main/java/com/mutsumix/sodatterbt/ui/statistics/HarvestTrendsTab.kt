package com.mutsumix.sodatterbt.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import androidx.compose.runtime.LaunchedEffect

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)

@Composable
fun HarvestTrendsTab(
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.monthlyData.isEmpty() && uiState.dailyData.isEmpty()) {
        EmptyState("収穫データがありません")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // フィルター + 合計
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.drillDownMonth != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.drillUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Muted)
                    }
                    Text(
                        uiState.drillDownMonth!!.replace("/", "年") + "月",
                        color = OnBackground,
                        fontSize = 14.sp,
                    )
                }
            } else {
                Text(
                    "合計: ${formatWeight(uiState.totalGram)}",
                    color = OnBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            VarietyFilterDropdown(
                varieties = uiState.varieties,
                selected = uiState.selectedVariety,
                onSelect = { viewModel.selectVariety(it) },
            )
        }

        // グラフ
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        ) {
            if (uiState.drillDownMonth != null) {
                DailyBarChart(uiState.dailyData)
            } else {
                MonthlyBarChart(
                    data = uiState.monthlyData,
                    onBarClick = { yearMonth -> viewModel.drillDown(yearMonth) },
                )
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(data: List<MonthlyHarvest>, onBarClick: (String) -> Unit) {
    if (data.isEmpty()) return

    val labels = remember(data) { data.map { it.yearMonth.split("/").last() + "月" } }
    val modelProducer = remember { CartesianChartModelProducer() }
    val formatter = remember(labels) {
        CartesianValueFormatter { _, x, _ -> labels.getOrElse(x.toInt()) { "" } }
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.map { it.totalGram }) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = fill(Secondary), thickness = 16.dp),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = formatter),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    )
}

@Composable
private fun DailyBarChart(data: List<DailyHarvest>) {
    if (data.isEmpty()) return

    val labels = remember(data) { data.map { it.date + "日" } }
    val modelProducer = remember { CartesianChartModelProducer() }
    val formatter = remember(labels) {
        CartesianValueFormatter { _, x, _ -> labels.getOrElse(x.toInt()) { "" } }
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.map { it.totalGram }) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = fill(Primary), thickness = 12.dp),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = formatter),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    )
}

@Composable
fun VarietyFilterDropdown(
    varieties: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Divider),
        ) {
            Text(selected ?: "すべて", color = Muted, fontSize = 12.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("すべて") },
                onClick = { onSelect(null); expanded = false },
            )
            varieties.forEach { variety ->
                DropdownMenuItem(
                    text = { Text(variety) },
                    onClick = { onSelect(variety); expanded = false },
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("📊", fontSize = 40.sp)
            Text(message, color = Muted, fontSize = 14.sp)
        }
    }
}

private fun formatWeight(gram: Float): String {
    return if (gram % 1f == 0f) "${gram.toInt()}g" else "${gram}g"
}
