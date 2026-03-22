package com.mutsumix.sodatterbt.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.db.dao.CultivationDao
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MonthlyHarvest(
    val yearMonth: String, // "2025/01"
    val totalGram: Float,
    val byVariety: Map<String, Float>,
)

data class DailyHarvest(
    val date: String, // "01", "02", ...
    val totalGram: Float,
    val byVariety: Map<String, Float>,
)

data class ScatterPoint(
    val cultivationDays: Int,
    val weightGram: Float,
    val varietyName: String,
)

data class StatisticsUiState(
    val monthlyData: List<MonthlyHarvest> = emptyList(),
    val dailyData: List<DailyHarvest> = emptyList(),
    val drillDownMonth: String? = null, // null = 月別表示、非null = その月の日別表示
    val scatterData: List<ScatterPoint> = emptyList(),
    val varieties: List<String> = emptyList(),
    val selectedVariety: String? = null, // null = すべて
    val totalGram: Float = 0f,
)

private val yearMonthFormat = SimpleDateFormat("yyyy/MM", Locale.JAPAN)
private val dayFormat = SimpleDateFormat("dd", Locale.JAPAN)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val cultivationDao: CultivationDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cultivationDao.getHarvestedForStatistics(),
                cultivationDao.getDistinctVarieties(),
            ) { records, varieties ->
                processData(records, varieties, _uiState.value.selectedVariety, _uiState.value.drillDownMonth)
            }.collect { _uiState.value = it }
        }
    }

    fun selectVariety(variety: String?) {
        _uiState.value = _uiState.value.copy(selectedVariety = variety)
        recalculate()
    }

    fun drillDown(yearMonth: String) {
        _uiState.value = _uiState.value.copy(drillDownMonth = yearMonth)
        recalculate()
    }

    fun drillUp() {
        _uiState.value = _uiState.value.copy(drillDownMonth = null)
        recalculate()
    }

    private fun recalculate() {
        viewModelScope.launch {
            cultivationDao.getHarvestedForStatistics().collect { records ->
                val varieties = _uiState.value.varieties
                _uiState.value = processData(records, varieties, _uiState.value.selectedVariety, _uiState.value.drillDownMonth)
            }
        }
    }

    private fun processData(
        records: List<CultivationEntity>,
        varieties: List<String>,
        selectedVariety: String?,
        drillDownMonth: String?,
    ): StatisticsUiState {
        val filtered = if (selectedVariety != null) {
            records.filter { it.varietyName == selectedVariety }
        } else {
            records
        }

        // 月別集計
        val monthlyMap = mutableMapOf<String, MutableMap<String, Float>>()
        filtered.forEach { record ->
            val harvestDate = record.harvestDate ?: return@forEach
            val weight = record.harvestWeightGram ?: return@forEach
            val ym = yearMonthFormat.format(Date(harvestDate))
            val varietyMap = monthlyMap.getOrPut(ym) { mutableMapOf() }
            varietyMap[record.varietyName] = (varietyMap[record.varietyName] ?: 0f) + weight
        }
        val monthlyData = monthlyMap.entries
            .sortedBy { it.key }
            .map { (ym, byVariety) ->
                MonthlyHarvest(ym, byVariety.values.sum(), byVariety)
            }

        // 日別集計（ドリルダウン時）
        val dailyData = if (drillDownMonth != null) {
            val dailyMap = mutableMapOf<String, MutableMap<String, Float>>()
            filtered.forEach { record ->
                val harvestDate = record.harvestDate ?: return@forEach
                val weight = record.harvestWeightGram ?: return@forEach
                val ym = yearMonthFormat.format(Date(harvestDate))
                if (ym == drillDownMonth) {
                    val day = dayFormat.format(Date(harvestDate))
                    val varietyMap = dailyMap.getOrPut(day) { mutableMapOf() }
                    varietyMap[record.varietyName] = (varietyMap[record.varietyName] ?: 0f) + weight
                }
            }
            dailyMap.entries
                .sortedBy { it.key }
                .map { (day, byVariety) ->
                    DailyHarvest(day, byVariety.values.sum(), byVariety)
                }
        } else {
            emptyList()
        }

        // 散布図データ
        val scatterData = filtered.mapNotNull { record ->
            val harvestDate = record.harvestDate ?: return@mapNotNull null
            val weight = record.harvestWeightGram ?: return@mapNotNull null
            val days = ((harvestDate - record.seedingDate) / 86_400_000L).toInt()
            ScatterPoint(days, weight, record.varietyName)
        }

        val totalGram = filtered.mapNotNull { it.harvestWeightGram }.sum()

        return StatisticsUiState(
            monthlyData = monthlyData,
            dailyData = dailyData,
            drillDownMonth = drillDownMonth,
            scatterData = scatterData,
            varieties = varieties,
            selectedVariety = selectedVariety,
            totalGram = totalGram,
        )
    }
}
