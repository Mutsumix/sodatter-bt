package com.mutsumix.sodatterbt.data.repository

import com.mutsumix.sodatterbt.data.db.dao.GrowthPhotoDao
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrowthPhotoRepository @Inject constructor(
    private val growthPhotoDao: GrowthPhotoDao,
) {
    fun getPhotosForCultivation(cultivationId: Long): Flow<List<GrowthPhotoEntity>> =
        growthPhotoDao.getPhotosForCultivation(cultivationId)

    suspend fun insert(photo: GrowthPhotoEntity): Long =
        growthPhotoDao.insert(photo)
}
