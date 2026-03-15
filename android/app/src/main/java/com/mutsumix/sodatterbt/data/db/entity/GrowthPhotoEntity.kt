package com.mutsumix.sodatterbt.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "growth_photos",
    foreignKeys = [
        ForeignKey(
            entity = CultivationEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivation_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cultivation_id")],
)
data class GrowthPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "cultivation_id") val cultivationId: Long,
    @ColumnInfo(name = "photo_uri") val photoUri: String,
    @ColumnInfo(name = "taken_at") val takenAt: Long,
    val note: String? = null,
)
