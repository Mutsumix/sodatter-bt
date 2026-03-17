package com.mutsumix.sodatterbt.ui.labelprint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import com.mutsumix.sodatterbt.data.repository.DeviceRepository
import com.mutsumix.sodatterbt.device.printer.LabelData
import com.mutsumix.sodatterbt.device.printer.PrinterState
import com.mutsumix.sodatterbt.device.printer.StarPrinterManager
import com.mutsumix.sodatterbt.navigation.LabelPrint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class LabelPrintUiState(
    val device: DeviceEntity? = null,
    val cultivation: CultivationEntity? = null,
    val currentWeightGram: Float = 0f,
    val printerConnected: Boolean = false,
    val isDiscovering: Boolean = false,
    val isPrinting: Boolean = false,
    val toastMessage: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class LabelPrintViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val cultivationRepository: CultivationRepository,
    private val printerManager: StarPrinterManager,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<LabelPrint>()
    private val deviceId: Int = route.deviceId

    private val _uiState = MutableStateFlow(LabelPrintUiState(currentWeightGram = route.weightGram))
    val uiState: StateFlow<LabelPrintUiState> = _uiState.asStateFlow()

    private var printerIdentifier: String? = null

    init {
        viewModelScope.launch {
            val device = deviceRepository.getById(deviceId)
            // アクティブ栽培優先、なければ収穫済みにフォールバック
            combine(
                cultivationRepository.getActiveCultivationByDevice(deviceId),
                cultivationRepository.getHarvestedCultivations(),
            ) { active, harvested ->
                active ?: harvested.firstOrNull { it.deviceId == deviceId }
            }.collect { cultivation ->
                _uiState.value = _uiState.value.copy(
                    device = device,
                    cultivation = cultivation,
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            printerManager.state.collect { state ->
                when (state) {
                    is PrinterState.Idle -> _uiState.value = _uiState.value.copy(
                        printerConnected = false, isDiscovering = false, isPrinting = false
                    )
                    is PrinterState.Discovering, PrinterState.Connecting -> _uiState.value = _uiState.value.copy(
                        isDiscovering = true, printerConnected = false
                    )
                    is PrinterState.Connected -> {
                        printerIdentifier = state.identifier
                        _uiState.value = _uiState.value.copy(
                            printerConnected = true, isDiscovering = false
                        )
                    }
                    is PrinterState.Printing -> _uiState.value = _uiState.value.copy(isPrinting = true)
                    is PrinterState.PrintSuccess -> _uiState.value = _uiState.value.copy(
                        isPrinting = false,
                        toastMessage = "ラベルをプリンターに送信しました",
                    )
                    is PrinterState.Error -> _uiState.value = _uiState.value.copy(
                        isPrinting = false,
                        isDiscovering = false,
                        toastMessage = state.message,
                    )
                }
            }
        }
    }

    fun connectPrinter() {
        printerManager.discover()
    }

    fun print() {
        val state = _uiState.value
        val cultivation = state.cultivation ?: run {
            _uiState.value = state.copy(toastMessage = "栽培データがありません")
            return
        }
        val device = state.device ?: return
        val identifier = printerIdentifier ?: run {
            _uiState.value = state.copy(toastMessage = "プリンターが接続されていません")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val effectiveHarvestDate = cultivation.harvestDate ?: System.currentTimeMillis()
        val daysElapsed = ((effectiveHarvestDate - cultivation.seedingDate) / 86_400_000L).toInt()

        val labelData = LabelData(
            cultivationId = cultivation.id,
            cropName = cultivation.varietyName,
            manufacturer = cultivation.manufacturer,
            seedingDate = dateFormat.format(Date(cultivation.seedingDate)),
            harvestDate = dateFormat.format(Date(effectiveHarvestDate)),
            weightGram = cultivation.harvestWeightGram ?: state.currentWeightGram,
            deviceName = device.name,
            daysElapsed = daysElapsed,
        )

        viewModelScope.launch {
            printerManager.print(identifier, labelData)
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        printerManager.reset()
    }
}
