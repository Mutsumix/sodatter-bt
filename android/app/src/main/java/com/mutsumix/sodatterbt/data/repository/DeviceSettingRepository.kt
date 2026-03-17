package com.mutsumix.sodatterbt.data.repository

import com.mutsumix.sodatterbt.data.db.dao.DeviceSettingDao
import com.mutsumix.sodatterbt.data.db.entity.DeviceSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

object SettingKey {
    const val ESP32_IP = "esp32_ip"
}

@Singleton
class DeviceSettingRepository @Inject constructor(
    private val deviceSettingDao: DeviceSettingDao,
) {
    fun observe(key: String): Flow<String?> =
        deviceSettingDao.observe(key).map { it?.value }

    suspend fun get(key: String): String? = deviceSettingDao.getValue(key)

    suspend fun set(key: String, value: String) =
        deviceSettingDao.upsert(DeviceSettingEntity(key, value))

    suspend fun delete(key: String) = deviceSettingDao.delete(key)
}
