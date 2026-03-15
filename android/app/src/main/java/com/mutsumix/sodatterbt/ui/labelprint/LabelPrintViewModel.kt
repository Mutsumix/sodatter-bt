package com.mutsumix.sodatterbt.ui.labelprint

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

data class LabelPrintUiState(
    val device: DeviceEntity? = null,
    val cultivation: CultivationEntity? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class LabelPrintViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
) : ViewModel() {

    private val deviceId: Int = savedStateHandle.toRoute<Routes.LabelPrint>().deviceId

    private val _uiState = MutableStateFlow(LabelPrintUiState())
    val uiState: StateFlow<LabelPrintUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val device = deviceRepository.getById(deviceId)
            // 収穫直後なので is_active=0 の最新レコードを使う
            // getHarvestedCultivations はデバイス絞り込みなしなので直接クエリは省略し、
            // active=false になっているものを harvest_date DESC で取る
            cultivationRepository.getHarvestedCultivations().collect { list ->
                val cultivation = list.firstOrNull { it.deviceId == deviceId }
                _uiState.value = LabelPrintUiState(
                    device = device,
                    cultivation = cultivation,
                    isLoading = false,
                )
            }
        }
    }
}
