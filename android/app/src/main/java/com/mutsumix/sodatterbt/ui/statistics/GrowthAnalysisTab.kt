package com.mutsumix.sodatterbt.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val OnBackground = Color(0xFF1A1A1C)
private val Muted = Color(0xFF6B6B6B)
private val Divider = Color(0xFFD4D4D4)
private val GridColor = Color(0xFFE8E8E8)

private val varietyColors = listOf(
    Color(0xFF6DAE72), // 緑
    Color(0xFF5B8BD4), // 青
    Color(0xFFE8A838), // オレンジ
    Color(0xFFD45B5B), // 赤
    Color(0xFF8B5BD4), // 紫
    Color(0xFF5BBDD4), // 水色
    Color(0xFFD4A85B), // 茶
)

@Composable
fun GrowthAnalysisTab(
    viewModel: StatisticsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.scatterData.isEmpty()) {
        EmptyState("収穫データがありません")
        return
    }

    val colorMap = remember(uiState.varieties) {
        uiState.varieties.mapIndexed { index, variety ->
            variety to varietyColors[index % varietyColors.size]
        }.toMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // フィルター
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            VarietyFilterDropdown(
                varieties = uiState.varieties,
                selected = uiState.selectedVariety,
                onSelect = { viewModel.selectVariety(it) },
            )
        }

        // 散布図
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Divider),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
            ScatterPlot(
                data = uiState.scatterData,
                colorMap = colorMap,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }

        // 軸ラベル
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("X: 栽培日数  Y: 収穫量 (g)", color = Muted, fontSize = 12.sp)
        }

        // 凡例
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7F7F7),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("凡例", color = Muted, fontSize = 12.sp)
                colorMap.forEach { (variety, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier = Modifier.size(10.dp),
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(variety, color = OnBackground, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScatterPlot(
    data: List<ScatterPoint>,
    colorMap: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    val maxDays = (data.maxOfOrNull { it.cultivationDays } ?: 100).let { (it * 1.1f).toInt().coerceAtLeast(10) }
    val maxWeight = (data.maxOfOrNull { it.weightGram } ?: 100f).let { it * 1.1f }

    Canvas(modifier = modifier) {
        val chartLeft = 48f
        val chartBottom = size.height - 32f
        val chartRight = size.width - 16f
        val chartTop = 16f
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // グリッド線
        for (i in 0..4) {
            val y = chartTop + chartHeight * i / 4
            drawLine(GridColor, Offset(chartLeft, y), Offset(chartRight, y), strokeWidth = 1f)
            // Y軸ラベル
            val label = ((maxWeight * (4 - i) / 4).toInt()).toString()
            drawContext.canvas.nativeCanvas.drawText(
                label, chartLeft - 8f, y + 4f, android.graphics.Paint().apply {
                    textSize = 24f; textAlign = android.graphics.Paint.Align.RIGHT; color = 0xFF6B6B6B.toInt()
                }
            )
        }

        // X軸ラベル
        for (i in 0..4) {
            val x = chartLeft + chartWidth * i / 4
            drawLine(GridColor, Offset(x, chartTop), Offset(x, chartBottom), strokeWidth = 1f)
            val label = (maxDays * i / 4).toString()
            drawContext.canvas.nativeCanvas.drawText(
                label, x, chartBottom + 24f, android.graphics.Paint().apply {
                    textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER; color = 0xFF6B6B6B.toInt()
                }
            )
        }

        // データポイント
        data.forEach { point ->
            val x = chartLeft + (point.cultivationDays.toFloat() / maxDays) * chartWidth
            val y = chartTop + (1f - point.weightGram / maxWeight) * chartHeight
            val color = colorMap[point.varietyName] ?: Color.Gray
            drawCircle(color, radius = 8f, center = Offset(x, y))
        }
    }
}
