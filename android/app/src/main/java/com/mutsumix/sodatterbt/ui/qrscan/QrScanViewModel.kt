package com.mutsumix.sodatterbt.ui.qrscan

import androidx.lifecycle.ViewModel
import com.mutsumix.sodatterbt.data.repository.CultivationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class QrScanViewModel @Inject constructor(
    private val cultivationRepository: CultivationRepository,
) : ViewModel() {

    /**
     * cultivationId から deviceId を解決する。
     * 見つからなければ null を返す。
     */
    suspend fun resolveDeviceId(cultivationId: Long): Int? {
        return cultivationRepository.getById(cultivationId).first()?.deviceId
    }
}
