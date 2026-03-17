package com.mutsumix.sodatterbt.data.repository

import com.mutsumix.sodatterbt.data.db.dao.CultivationDao
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CultivationRepository @Inject constructor(
    private val cultivationDao: CultivationDao,
) {
    fun getActiveCultivationByDevice(deviceId: Int): Flow<CultivationEntity?> =
        cultivationDao.getActiveCultivationByDevice(deviceId)

    fun getById(id: Long): Flow<CultivationEntity?> = cultivationDao.getById(id)

    fun getHarvestedCultivations(): Flow<List<CultivationEntity>> =
        cultivationDao.getHarvestedCultivations()

    suspend fun insert(cultivation: CultivationEntity): Long =
        cultivationDao.insert(cultivation)

    suspend fun delete(cultivationId: Long) = cultivationDao.deleteById(cultivationId)

    suspend fun recordHarvest(cultivationId: Long, weightGram: Float, harvestDate: Long) {
        val flow = cultivationDao.getById(cultivationId)
        // 1回だけ取得して更新
        flow.first()?.let { current ->
            cultivationDao.update(
                current.copy(
                    harvestDate = harvestDate,
                    harvestWeightGram = weightGram,
                    isActive = false,
                )
            )
        }
    }
}
