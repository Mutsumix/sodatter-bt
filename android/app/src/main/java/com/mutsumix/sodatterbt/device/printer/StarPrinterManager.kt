package com.mutsumix.sodatterbt.device.printer

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarIO10CommunicationException
import com.starmicronics.stario10.StarIO10NotFoundException
import com.starmicronics.stario10.StarIO10UnprintableException
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.CharacterEncodingType
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeLevel
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeParameter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class PrinterState {
    object Idle : PrinterState()
    object Discovering : PrinterState()
    object Connecting : PrinterState()
    data class Connected(val identifier: String) : PrinterState()
    object Printing : PrinterState()
    object PrintSuccess : PrinterState()
    data class Error(val message: String) : PrinterState()
}

data class LabelData(
    val cultivationId: Long,
    val cropName: String,
    val manufacturer: String,
    val seedingDate: String,
    val harvestDate: String,
    val weightGram: Float,
    val deviceName: String,
    val daysElapsed: Int,
)

@Singleton
class StarPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow<PrinterState>(PrinterState.Idle)
    val state: StateFlow<PrinterState> = _state.asStateFlow()

    fun discover() {
        _state.value = PrinterState.Discovering
        try {
            val manager = StarDeviceDiscoveryManagerFactory.create(
                listOf(InterfaceType.Bluetooth),
                context,
            )
            manager.discoveryTime = 10_000
            manager.callback = object : StarDeviceDiscoveryManager.Callback {
                override fun onPrinterFound(printer: StarPrinter) {
                    _state.value = PrinterState.Connected(
                        printer.connectionSettings.identifier
                    )
                }

                override fun onDiscoveryFinished() {
                    if (_state.value is PrinterState.Discovering) {
                        _state.value = PrinterState.Error("プリンターが見つかりませんでした")
                    }
                }
            }
            manager.startDiscovery()
        } catch (e: Exception) {
            _state.value = PrinterState.Error(e.message ?: "Discovery失敗")
        }
    }

    suspend fun print(identifier: String, data: LabelData) {
        _state.value = PrinterState.Printing
        val settings = StarConnectionSettings(InterfaceType.Bluetooth, identifier)
        val printer = StarPrinter(settings, context)
        try {
            printer.openAsync().await()

            val qrContent = "sodatterbt://cultivation/${data.cultivationId}"
            val builder = StarXpandCommandBuilder()
            builder.addDocument(
                DocumentBuilder().addPrinter(
                    PrinterBuilder()
                        .styleSecondPriorityCharacterEncoding(CharacterEncodingType.Japanese)
                        .actionPrintText("${data.cropName}\n")
                        .actionPrintText("${data.manufacturer}\n")
                        .actionPrintText("播種: ${data.seedingDate}\n")
                        .actionPrintText("収穫: ${data.harvestDate}\n")
                        .actionPrintText("重量: ${data.weightGram}g\n")
                        .actionPrintText("装置${data.deviceName} Day${data.daysElapsed}\n")
                        .actionFeedLine(1)
                        .actionPrintQRCode(
                            QRCodeParameter(qrContent)
                                .setLevel(QRCodeLevel.L)
                                .setCellSize(8)
                        )
                        .actionFeedLine(2)
                        .actionCut(CutType.Partial)
                )
            )
            printer.printAsync(builder.getCommands()).await()
            _state.value = PrinterState.PrintSuccess
        } catch (e: StarIO10UnprintableException) {
            _state.value = PrinterState.Error("印刷不可: ${e.message}")
        } catch (e: StarIO10CommunicationException) {
            _state.value = PrinterState.Error("通信エラー: ${e.message}")
        } catch (e: StarIO10NotFoundException) {
            _state.value = PrinterState.Error("プリンターが見つかりません")
        } catch (e: Exception) {
            _state.value = PrinterState.Error(e.message ?: "不明なエラー")
        } finally {
            printer.closeAsync().await()
        }
    }

    fun reset() {
        _state.value = PrinterState.Idle
    }
}
