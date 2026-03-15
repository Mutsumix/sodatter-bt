package com.mutsumix.sodatterbt.ui.photorecord

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.data.repository.GrowthPhotoRepository
import com.mutsumix.sodatterbt.navigation.PhotoRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoRecordUiState(
    val device: DeviceEntity? = null,
    val cropName: String = "",
    val daysElapsed: Int = 0,
    val recentPhotoDates: List<Long> = emptyList(),
    val isSaved: Boolean = false,
)

@HiltViewModel
class PhotoRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val growthPhotoRepository: GrowthPhotoRepository,
) : ViewModel() {

    private val deviceId: Int = savedStateHandle.toRoute<PhotoRecord>().deviceId

    private val _uiState = MutableStateFlow(PhotoRecordUiState())
    val uiState: StateFlow<PhotoRecordUiState> = _uiState.asStateFlow()

    private var cultivationId: Long? = null

    init {
        viewModelScope.launch {
            val device = deviceRepository.getById(deviceId)
            val cultivation = cultivationRepository.getActiveCultivationByDevice(deviceId).first()
            if (cultivation != null) {
                cultivationId = cultivation.id
                val photos = growthPhotoRepository.getPhotosForCultivation(cultivation.id).first()
                val days = ((System.currentTimeMillis() - cultivation.seedingDate) / 86_400_000L).toInt()
                _uiState.value = PhotoRecordUiState(
                    device = device,
                    cropName = cultivation.varietyName,
                    daysElapsed = days,
                    recentPhotoDates = photos.takeLast(3).map { it.takenAt },
                )
            }
        }
    }

    fun savePhoto(photoUri: String) {
        val cultId = cultivationId ?: return
        viewModelScope.launch {
            growthPhotoRepository.insert(
                GrowthPhotoEntity(
                    cultivationId = cultId,
                    photoUri = photoUri,
                    takenAt = System.currentTimeMillis(),
                )
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
