package com.mutsumix.sodatterbt.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutsumix.sodatterbt.data.repository.DeviceWithCultivation
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val devices: List<DeviceWithCultivation> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceRepository: DeviceRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = deviceRepository
        .getAllDevicesWithCultivation()
        .map { HomeUiState(devices = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
