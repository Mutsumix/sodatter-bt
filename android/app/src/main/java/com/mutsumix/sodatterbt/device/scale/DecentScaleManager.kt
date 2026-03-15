package com.mutsumix.sodatterbt.device.scale

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val UUID_NOTIFY = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
private val UUID_WRITE = UUID.fromString("000036F5-0000-1000-8000-00805F9B34FB")
private val UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

private val CMD_TARE = byteArrayOf(0x03, 0x0F, 0x00, 0x00, 0x00, 0x01, 0x0E)
private val CMD_LED_ON = byteArrayOf(0x03, 0x0A.toByte(), 0x01, 0x01, 0x00, 0x01, 0x08)

sealed class ScaleState {
    object Idle : ScaleState()
    object Scanning : ScaleState()
    object Connecting : ScaleState()
    data class Connected(val weightGram: Float) : ScaleState()
    data class Error(val message: String) : ScaleState()
}

@Singleton
@SuppressLint("MissingPermission")
class DecentScaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private val _state = MutableStateFlow<ScaleState>(ScaleState.Idle)
    val state: StateFlow<ScaleState> = _state.asStateFlow()

    private var gatt: BluetoothGatt? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
            _state.value = ScaleState.Connecting
            result.device.connectGatt(context, false, gattCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            _state.value = ScaleState.Error("スキャン失敗: エラーコード $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                this@DecentScaleManager.gatt = gatt
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _state.value = ScaleState.Idle
                gatt.close()
                this@DecentScaleManager.gatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val characteristic = gatt.services
                .flatMap { it.characteristics }
                .firstOrNull { it.uuid == UUID_NOTIFY } ?: run {
                _state.value = ScaleState.Error("Notifyキャラクタリスティックが見つかりません")
                return
            }

            // Notification有効化
            gatt.setCharacteristicNotification(characteristic, true)
            characteristic.getDescriptor(UUID_CCC)?.let { desc ->
                @Suppress("DEPRECATION")
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(desc)
            }

            // LED ONコマンド送信 (APPモード移行)
            gatt.services
                .flatMap { it.characteristics }
                .firstOrNull { it.uuid == UUID_WRITE }
                ?.let { writeChar ->
                    @Suppress("DEPRECATION")
                    writeChar.value = CMD_LED_ON
                    @Suppress("DEPRECATION")
                    gatt.writeCharacteristic(writeChar)
                }
        }

        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (characteristic.uuid == UUID_NOTIFY) {
                val data = characteristic.value ?: return
                val weight = parseWeight(data)
                _state.value = ScaleState.Connected(weight)
            }
        }
    }

    fun startScan(savedIdentifier: String? = null) {
        val adapter = bluetoothAdapter ?: run {
            _state.value = ScaleState.Error("Bluetoothが利用できません")
            return
        }

        // 保存済みMACアドレスがあれば直接接続
        if (savedIdentifier != null) {
            _state.value = ScaleState.Connecting
            try {
                adapter.getRemoteDevice(savedIdentifier)
                    .connectGatt(context, false, gattCallback)
                return
            } catch (_: IllegalArgumentException) { /* fall through to scan */ }
        }

        _state.value = ScaleState.Scanning
        val filter = ScanFilter.Builder().setDeviceName("Decent Scale").build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
    }

    fun tare() {
        val writeChar = gatt?.services
            ?.flatMap { it.characteristics }
            ?.firstOrNull { it.uuid == UUID_WRITE } ?: return
        @Suppress("DEPRECATION")
        writeChar.value = CMD_TARE
        @Suppress("DEPRECATION")
        gatt?.writeCharacteristic(writeChar)
    }

    fun disconnect() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _state.value = ScaleState.Idle
    }

    companion object {
        fun parseWeight(data: ByteArray): Float {
            if (data.size < 7) return 0f
            val highByte = data[2].toInt() and 0xFF
            val lowByte = data[3].toInt() and 0xFF
            val rawWeight = (highByte shl 8) or lowByte
            val signedWeight = if (rawWeight > 32767) rawWeight - 65536 else rawWeight
            return signedWeight / 10.0f
        }
    }
}
