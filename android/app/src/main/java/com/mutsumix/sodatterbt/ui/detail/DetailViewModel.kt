package com.mutsumix.sodatterbt.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.data.repository.GrowthPhotoRepository
import com.mutsumix.sodatterbt.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val device: DeviceEntity? = null,
    val cultivation: CultivationEntity? = null,
    val growthPhotos: List<GrowthPhotoEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val growthPhotoRepository: GrowthPhotoRepository,
) : ViewModel() {

    private val deviceId: Int = savedStateHandle.toRoute<Routes.Detail>().deviceId

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activeCultivationFlow = cultivationRepository.getActiveCultivationByDevice(deviceId)

            activeCultivationFlow
                .combine(
                    activeCultivationFlow
                        .filterNotNull()
                        .flatMapLatest { cultivation ->
                            growthPhotoRepository.getPhotosForCultivation(cultivation.id)
                        }
                ) { cultivation, photos ->
                    val device = deviceRepository.getById(deviceId)
                    DetailUiState(
                        device = device,
                        cultivation = cultivation,
                        growthPhotos = photos,
                        isLoading = false,
                    )
                }
                .collect { _uiState.value = it }
        }
    }
}
