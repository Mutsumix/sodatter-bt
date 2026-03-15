package com.mutsumix.sodatterbt.data.repository

import com.mutsumix.sodatterbt.data.db.dao.CultivationDao
import com.mutsumix.sodatterbt.data.db.dao.DeviceDao
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceWithCultivation(
    val device: DeviceEntity,
    val cultivation: CultivationEntity?,
)

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao,
    private val cultivationDao: CultivationDao,
) {
    fun getAllDevicesWithCultivation(): Flow<List<DeviceWithCultivation>> =
        deviceDao.getAllDevices().combine(cultivationDao.getActiveCultivations()) { devices, active ->
            devices.map { device ->
                DeviceWithCultivation(
                    device = device,
                    cultivation = active.firstOrNull { it.deviceId == device.id },
                )
            }
        }

    suspend fun getById(id: Int): DeviceEntity? = deviceDao.getById(id)

    suspend fun updateTagMac(deviceId: Int, macAddress: String?) {
        val device = deviceDao.getById(deviceId) ?: return
        deviceDao.update(device.copy(tagMacAddress = macAddress))
    }
}
