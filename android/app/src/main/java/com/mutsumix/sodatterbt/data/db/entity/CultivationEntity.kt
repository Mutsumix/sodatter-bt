package com.mutsumix.sodatterbt.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cultivations",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("device_id")],
)
data class CultivationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    @ColumnInfo(name = "variety_name") val varietyName: String,
    val manufacturer: String,
    @ColumnInfo(name = "seeding_date") val seedingDate: Long,
    @ColumnInfo(name = "harvest_date") val harvestDate: Long? = null,
    @ColumnInfo(name = "harvest_weight_gram") val harvestWeightGram: Float? = null,
    @ColumnInfo(name = "seed_photo_uri") val seedPhotoUri: String? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
