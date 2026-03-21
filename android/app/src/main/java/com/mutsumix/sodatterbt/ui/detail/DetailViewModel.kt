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
import com.mutsumix.sodatterbt.data.repository.DeviceSettingRepository
import com.mutsumix.sodatterbt.data.repository.GrowthPhotoRepository
import com.mutsumix.sodatterbt.data.repository.SettingKey
import com.mutsumix.sodatterbt.device.epaper.EpaperApiClient
import com.mutsumix.sodatterbt.device.epaper.EpaperResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mutsumix.sodatterbt.navigation.Detail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
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
    val isDeleted: Boolean = false,
    val canUpdateEpaper: Boolean = false,
    val isUpdatingEpaper: Boolean = false,
    val epaperMessage: String? = null,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val growthPhotoRepository: GrowthPhotoRepository,
    private val settingRepository: DeviceSettingRepository,
    private val epaperApiClient: EpaperApiClient,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Detail>()
    private val deviceId: Int = route.deviceId

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // cultivationId指定あり → そのIDで取得、なし → デバイスのアクティブ栽培
            val cultivationFlow: Flow<CultivationEntity?> = if (route.cultivationId >= 0L) {
                cultivationRepository.getById(route.cultivationId)
            } else {
                cultivationRepository.getActiveCultivationByDevice(deviceId)
            }

            combine(
                cultivationFlow,
                cultivationFlow
                    .filterNotNull()
                    .flatMapLatest { cultivation ->
                        growthPhotoRepository.getPhotosForCultivation(cultivation.id)
                    },
                settingRepository.observeBoolean(SettingKey.BT_EPAPER_ENABLED),
            ) { cultivation, photos, epaperEnabled ->
                val device = deviceRepository.getById(deviceId)
                val esp32Ip = settingRepository.get(SettingKey.ESP32_IP)
                val hasEpaper = !esp32Ip.isNullOrBlank() && !device?.tagMacAddress.isNullOrBlank()
                DetailUiState(
                    device = device,
                    cultivation = cultivation,
                    growthPhotos = photos,
                    isLoading = false,
                    canUpdateEpaper = epaperEnabled && hasEpaper && cultivation != null && route.cultivationId < 0L,
                )
            }
                .collect { _uiState.value = it }
        }
    }

    fun updateEpaperTag() {
        val state = _uiState.value
        val cultivation = state.cultivation ?: return
        val device = state.device ?: return
        val tagMac = device.tagMacAddress ?: return
        viewModelScope.launch {
            val esp32Ip = settingRepository.get(SettingKey.ESP32_IP) ?: return@launch
            _uiState.value = _uiState.value.copy(isUpdatingEpaper = true, epaperMessage = null)
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
            val result = epaperApiClient.updateTag(
                apIpAddress = esp32Ip,
                tagMacAddress = tagMac,
                cultivationId = cultivation.id,
                cropName = cultivation.varietyName,
                manufacturer = cultivation.manufacturer,
                seedingDate = dateFormat.format(Date(cultivation.seedingDate)),
                deviceName = device.name,
            )
            val message = when (result) {
                is EpaperResult.Success -> "電子ペーパータグを更新しました"
                is EpaperResult.Failure -> "タグ更新に失敗: ${result.message}"
            }
            _uiState.value = _uiState.value.copy(isUpdatingEpaper = false, epaperMessage = message)
        }
    }

    fun updateSeedPhoto(uri: String) {
        val cultivationId = _uiState.value.cultivation?.id ?: return
        viewModelScope.launch {
            cultivationRepository.updateSeedPhotoUri(cultivationId, uri)
        }
    }

    fun clearEpaperMessage() {
        _uiState.value = _uiState.value.copy(epaperMessage = null)
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            growthPhotoRepository.delete(photoId)
        }
    }

    fun deleteCultivation() {
        val cultivationId = _uiState.value.cultivation?.id ?: return
        viewModelScope.launch {
            cultivationRepository.delete(cultivationId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
