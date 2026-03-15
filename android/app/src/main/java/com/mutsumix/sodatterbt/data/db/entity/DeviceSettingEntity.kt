package com.mutsumix.sodatterbt.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_settings")
data class DeviceSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)
