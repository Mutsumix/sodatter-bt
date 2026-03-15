package com.mutsumix.sodatterbt.ui.seeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeedingUiState(
    val devices: List<DeviceEntity> = emptyList(),
    val selectedDeviceId: Int? = null,
    val variety: String = "",
    val manufacturer: String = "",
    val seedingDateMillis: Long = System.currentTimeMillis(),
    val deviceError: String = "",
    val varietyError: String = "",
    val isSaved: Boolean = false,
    val savedDeviceName: String = "",
)

@HiltViewModel
class SeedingViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeedingUiState())
    val uiState: StateFlow<SeedingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.getAllDevicesWithCultivation().collect { devicesWithCultivation ->
                _uiState.value = _uiState.value.copy(
                    devices = devicesWithCultivation.map { it.device }
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
            cultivationRepository.insert(
                CultivationEntity(
                    deviceId = deviceId,
                    varietyName = state.variety,
                    manufacturer = state.manufacturer,
                    seedingDate = state.seedingDateMillis,
                )
            )
            _uiState.value = _uiState.value.copy(isSaved = true, savedDeviceName = device.name)
        }
    }
}
