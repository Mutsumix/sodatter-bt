package com.mutsumix.sodatterbt.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthPhotoDao {
    @Query("SELECT * FROM growth_photos WHERE cultivation_id = :cultivationId ORDER BY taken_at ASC")
    fun getPhotosForCultivation(cultivationId: Long): Flow<List<GrowthPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(photo: GrowthPhotoEntity): Long
}
