package com.mutsumix.sodatterbt.ui.harvest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.data.repository.DeviceSettingRepository
import com.mutsumix.sodatterbt.data.repository.SettingKey
import com.mutsumix.sodatterbt.device.scale.DecentScaleManager
import com.mutsumix.sodatterbt.device.scale.ScaleState
import com.mutsumix.sodatterbt.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HarvestUiState(
    val device: DeviceEntity? = null,
    val cultivation: CultivationEntity? = null,
    val weightGram: Float = 0f,
    val scaleConnected: Boolean = false,
    val isScanning: Boolean = false,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class HarvestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val settingRepository: DeviceSettingRepository,
    private val scaleManager: DecentScaleManager,
) : ViewModel() {

    private val deviceId: Int = savedStateHandle.toRoute<Routes.Harvest>().deviceId

    private val _uiState = MutableStateFlow(HarvestUiState())
    val uiState: StateFlow<HarvestUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val device = deviceRepository.getById(deviceId)
            cultivationRepository.getActiveCultivationByDevice(deviceId).collect { cultivation ->
                _uiState.value = _uiState.value.copy(
                    device = device,
                    cultivation = cultivation,
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            scaleManager.state.collect { scaleState ->
                when (scaleState) {
                    is ScaleState.Idle -> _uiState.value = _uiState.value.copy(
                        scaleConnected = false, isScanning = false
                    )
                    is ScaleState.Scanning, ScaleState.Connecting -> _uiState.value = _uiState.value.copy(
                        isScanning = true, scaleConnected = false
                    )
                    is ScaleState.Connected -> _uiState.value = _uiState.value.copy(
                        scaleConnected = true,
                        isScanning = false,
                        weightGram = scaleState.weightGram,
                    )
                    is ScaleState.Error -> _uiState.value = _uiState.value.copy(
                        scaleConnected = false, isScanning = false, error = scaleState.message
                    )
                }
            }
        }
    }

    fun connectScale() {
        viewModelScope.launch {
            val identifier = settingRepository.get(SettingKey.SCALE_IDENTIFIER)
            scaleManager.startScan(identifier)
        }
    }

    fun tare() = scaleManager.tare()

    fun complete() {
        val state = _uiState.value
        val cultivation = state.cultivation ?: return
        viewModelScope.launch {
            cultivationRepository.recordHarvest(
                cultivationId = cultivation.id,
                weightGram = state.weightGram,
                harvestDate = System.currentTimeMillis(),
            )
            scaleManager.disconnect()
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        scaleManager.disconnect()
    }
}
