package com.mutsumix.sodatterbt.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HarvestRecord(
    val cultivation: CultivationEntity,
    val deviceName: String,
)

data class HistoryUiState(
    val records: List<HarvestRecord> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    cultivationRepository: CultivationRepository,
    deviceRepository: DeviceRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        cultivationRepository.getHarvestedCultivations(),
        deviceRepository.getAllDevicesWithCultivation(),
    ) { cultivations, devicesWithCultivation ->
        val deviceMap = devicesWithCultivation.associate { it.device.id to it.device.name }
        HistoryUiState(
            records = cultivations.map { cultivation ->
                HarvestRecord(
                    cultivation = cultivation,
                    deviceName = deviceMap[cultivation.deviceId] ?: "",
                )
            },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )
}
