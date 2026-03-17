package com.mutsumix.sodatterbt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.data.repository.DeviceSettingRepository
import com.mutsumix.sodatterbt.data.repository.SettingKey
import com.mutsumix.sodatterbt.device.epaper.EpaperApiClient
import com.mutsumix.sodatterbt.device.epaper.EpaperTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val devices: List<DeviceEntity> = emptyList(),
    val scaleIdentifier: String = "",
    val printerIdentifier: String = "",
    val esp32Ip: String = "",
    val availableTags: List<EpaperTag> = emptyList(),
    val isFetchingTags: Boolean = false,
    val tagFetchError: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val settingRepository: DeviceSettingRepository,
    private val epaperApiClient: EpaperApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                deviceRepository.getAllDevicesWithCultivation(),
                settingRepository.observe(SettingKey.SCALE_IDENTIFIER),
                settingRepository.observe(SettingKey.PRINTER_IDENTIFIER),
                settingRepository.observe(SettingKey.ESP32_IP),
            ) { devicesWithCultivation, scale, printer, esp32 ->
                SettingsUiState(
                    devices = devicesWithCultivation.map { it.device },
                    scaleIdentifier = scale ?: "",
                    printerIdentifier = printer ?: "",
                    esp32Ip = esp32 ?: "",
                )
            }.collect { _uiState.value = it }
        }
    }

    fun saveScaleIdentifier(value: String) {
        viewModelScope.launch { settingRepository.set(SettingKey.SCALE_IDENTIFIER, value) }
    }

    fun savePrinterIdentifier(value: String) {
        viewModelScope.launch { settingRepository.set(SettingKey.PRINTER_IDENTIFIER, value) }
    }

    fun saveEsp32Ip(value: String) {
        viewModelScope.launch { settingRepository.set(SettingKey.ESP32_IP, value) }
    }

    fun updateTagMac(deviceId: Int, macAddress: String?) {
        viewModelScope.launch { deviceRepository.updateTagMac(deviceId, macAddress) }
    }

    fun fetchAvailableTags() {
        val ip = _uiState.value.esp32Ip
        if (ip.isBlank()) {
            _uiState.value = _uiState.value.copy(tagFetchError = "ESP32 IPアドレスが未設定です")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingTags = true, tagFetchError = null)
            val tags = epaperApiClient.fetchTags(ip)
            _uiState.value = _uiState.value.copy(
                availableTags = tags,
                isFetchingTags = false,
                tagFetchError = if (tags.isEmpty()) "タグが見つかりませんでした" else null,
            )
        }
    }
}
