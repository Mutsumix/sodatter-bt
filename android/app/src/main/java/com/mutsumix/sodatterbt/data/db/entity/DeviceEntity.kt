package com.mutsumix.sodatterbt.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "tag_mac_address") val tagMacAddress: String? = null,
)
