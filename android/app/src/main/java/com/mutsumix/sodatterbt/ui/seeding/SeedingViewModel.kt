package com.mutsumix.sodatterbt.ui.seeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.data.repository.DeviceSettingRepository
import com.mutsumix.sodatterbt.data.repository.SettingKey
import com.mutsumix.sodatterbt.device.epaper.EpaperApiClient
import com.mutsumix.sodatterbt.device.epaper.EpaperResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DeviceOption(val id: Int, val name: String, val inUse: Boolean)

data class SeedingUiState(
    val deviceOptions: List<DeviceOption> = emptyList(),
    val selectedDeviceId: Int? = null,
    val variety: String = "",
    val manufacturer: String = "",
    val seedingDateMillis: Long = System.currentTimeMillis(),
    val deviceError: String = "",
    val varietyError: String = "",
    val isSaved: Boolean = false,
    val savedDeviceName: String = "",
    val isTagUpdating: Boolean = false,
    val tagUpdateMessage: String? = null,
)

@HiltViewModel
class SeedingViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val settingRepository: DeviceSettingRepository,
    private val epaperApiClient: EpaperApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeedingUiState())
    val uiState: StateFlow<SeedingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.getAllDevicesWithCultivation().collect { devicesWithCultivation ->
                _uiState.value = _uiState.value.copy(
                    deviceOptions = devicesWithCultivation.map { item ->
                        DeviceOption(
                            id = item.device.id,
                            name = item.device.name,
                            inUse = item.cultivation != null,
                        )
                    }
                )
            }
        }
    }

    fun selectDevice(deviceId: Int) {
        _uiState.value = _uiState.value.copy(selectedDeviceId = deviceId, deviceError = "")
    }

    fun setVariety(value: String) {
        _uiState.value = _uiState.value.copy(variety = value, varietyError = "")
    }

    fun setManufacturer(value: String) {
        _uiState.value = _uiState.value.copy(manufacturer = value)
    }

    fun setSeedingDate(millis: Long) {
        _uiState.value = _uiState.value.copy(seedingDateMillis = millis)
    }

    fun clearTagUpdateMessage() {
        _uiState.value = _uiState.value.copy(tagUpdateMessage = null)
    }

    /**
     * 選択中デバイスにタグ連携が設定されているか判定する
     */
    suspend fun hasTagLink(): Boolean {
        val deviceId = _uiState.value.selectedDeviceId ?: return false
        val device = deviceRepository.getById(deviceId) ?: return false
        val esp32Ip = settingRepository.get(SettingKey.ESP32_IP)
        return !esp32Ip.isNullOrBlank() && !device.tagMacAddress.isNullOrBlank()
    }

    fun register() {
        val state = _uiState.value
        var hasError = false

        if (state.selectedDeviceId == null) {
            _uiState.value = state.copy(deviceError = "デバイスを選択してください")
            hasError = true
        }
        if (state.variety.isBlank()) {
            _uiState.value = _uiState.value.copy(varietyError = "品種名を入力してください")
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val deviceId = state.selectedDeviceId!!
            val device = deviceRepository.getById(deviceId) ?: return@launch
            val cultivationId = cultivationRepository.insert(
                CultivationEntity(
                    deviceId = deviceId,
                    varietyName = state.variety,
                    manufacturer = state.manufacturer,
                    seedingDate = state.seedingDateMillis,
                )
            )
            _uiState.value = _uiState.value.copy(isSaved = true, savedDeviceName = device.name)

            // 電子ペーパータグ更新 (ESP32が設定済みの場合のみ)
            val esp32Ip = settingRepository.get(SettingKey.ESP32_IP)
            val tagMac = device.tagMacAddress
            if (!esp32Ip.isNullOrBlank() && !tagMac.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isTagUpdating = true)
                val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
                    .format(Date(state.seedingDateMillis))
                val result = epaperApiClient.updateTag(
                    apIpAddress = esp32Ip,
                    tagMacAddress = tagMac,
                    cultivationId = cultivationId,
                    cropName = state.variety,
                    manufacturer = state.manufacturer,
                    seedingDate = dateStr,
                    deviceName = device.name,
                )
                val message = when (result) {
                    is EpaperResult.Success -> "電子ペーパータグを更新しました"
                    is EpaperResult.Failure -> "タグ更新に失敗: ${result.message}"
                }
                _uiState.value = _uiState.value.copy(isTagUpdating = false, tagUpdateMessage = message)
            }
        }
    }
}
