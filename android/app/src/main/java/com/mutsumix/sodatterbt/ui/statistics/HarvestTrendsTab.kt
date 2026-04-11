package com.mutsumix.sodatterbt.ui.statistics

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val Primary = Color(0xFF5B8BD4)
private val Secondary = Color(0xFF6DAE72)
private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val DividerColor = Color(0xFFD4D4D4)
private val CardBorder = Color(0xFFE8E8E8)

private val varietyChartColors = listOf(
    Color(0xFF6DAE72),
    Color(0xFFA8D86C),
    Color(0xFFE8A07A),
    Color(0xFF2E7D32),
    Color(0xFFE57B9D),
)

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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 合計サマリカード
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (uiState.drillDownMonth != null)
                            "${uiState.drillDownMonth!!.replace("/", "年")}月 合計"
                        else "表示期間合計",
                        color = Muted,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatWeight(uiState.totalGram)}",
                        color = OnBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                VarietyFilterDropdown(
                    varieties = uiState.varieties,
                    selected = uiState.selectedVariety,
                    onSelect = { viewModel.selectVariety(it) },
                )
            }
        }

        // グラフカード
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ドリルダウンヘッダー
                if (uiState.drillDownMonth != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        IconButton(
                            onClick = { viewModel.drillUp() },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${uiState.drillDownMonth!!.replace("/", "年")}月 — 日別",
                            color = OnBackground,
                            fontSize = 14.sp,
                        )
                    }
                }

                // グラフ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                ) {
                    if (uiState.drillDownMonth != null) {
                        BarChart(
                            values = uiState.dailyData.map { it.totalGram },
                            labels = uiState.dailyData.map { it.date + "日" },
                            barColor = Primary,
                        )
                    } else {
                        BarChart(
                            values = uiState.monthlyData.map { it.totalGram },
                            labels = uiState.monthlyData.map { it.yearMonth.split("/").last() + "月" },
                            barColor = Secondary,
                        )
                    }
                }

                if (uiState.drillDownMonth == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "棒グラフをタップすると日別表示に切り替わります",
                        color = Color(0xFFABABAB),
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // 品種別合計カード（フィルターが「すべて」かつ月別表示時のみ）
        if (uiState.selectedVariety == null && uiState.drillDownMonth == null && uiState.varietyTotals.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("品種別合計", color = Muted, fontSize = 12.sp)
                    uiState.varietyTotals.forEachIndexed { index, vt ->
                        val color = varietyChartColors[index % varietyChartColors.size]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color,
                                modifier = Modifier.size(10.dp),
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                vt.varietyName,
                                color = OnBackground,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                            )
                            // バー
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFF0F0F0)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(vt.percentage)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                formatWeight(vt.totalGram),
                                color = Muted,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChart(
    values: List<Float>,
    labels: List<String>,
    barColor: Color,
) {
    if (values.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val yFormatter = remember {
        CartesianValueFormatter { _, y, _ -> "${y.toInt()}g" }
    }
    val xFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ -> labels.getOrElse(x.toInt()) { "" } }
    }

    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = fill(barColor), thickness = 16.dp),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = yFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxSize(),
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
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, DividerColor),
        ) {
            Text(selected ?: "全品種", color = OnBackground, fontSize = 12.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("全品種") },
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
