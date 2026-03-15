package com.mutsumix.sodatterbt.ui.harvest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
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
    val isConnectingScale: Boolean = false,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HarvestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
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
    }

    fun updateWeight(gram: Float) {
        _uiState.value = _uiState.value.copy(weightGram = gram)
    }

    fun complete() {
        val state = _uiState.value
        val cultivation = state.cultivation ?: return
        viewModelScope.launch {
            cultivationRepository.recordHarvest(
                cultivationId = cultivation.id,
                weightGram = state.weightGram,
                harvestDate = System.currentTimeMillis(),
            )
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}
