package com.mutsumix.sodatterbt.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mutsumix.sodatterbt.data.db.entity.DeviceSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSettingDao {
    @Query("SELECT * FROM device_settings WHERE `key` = :key LIMIT 1")
    fun observe(key: String): Flow<DeviceSettingEntity?>

    @Query("SELECT value FROM device_settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    // onConflict = REPLACE 必須: デフォルトABORTでは既存キー上書きがクラッシュになる
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: DeviceSettingEntity)

    @Query("DELETE FROM device_settings WHERE `key` = :key")
    suspend fun delete(key: String)
}
