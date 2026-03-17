package com.mutsumix.sodatterbt.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivationDao {
    // ホーム画面: 全装置のアクティブ栽培記録
    @Query("SELECT * FROM cultivations WHERE is_active = 1")
    fun getActiveCultivations(): Flow<List<CultivationEntity>>

    // 装置の現在のアクティブ栽培記録
    @Query("SELECT * FROM cultivations WHERE device_id = :deviceId AND is_active = 1 LIMIT 1")
    fun getActiveCultivationByDevice(deviceId: Int): Flow<CultivationEntity?>

    // IDで1件取得
    @Query("SELECT * FROM cultivations WHERE id = :id")
    fun getById(id: Long): Flow<CultivationEntity?>

    // 履歴一覧: 収穫済みを新しい順に
    @Query("SELECT * FROM cultivations WHERE is_active = 0 ORDER BY harvest_date DESC")
    fun getHarvestedCultivations(): Flow<List<CultivationEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cultivation: CultivationEntity): Long

    @Update
    suspend fun update(cultivation: CultivationEntity)

    @Query("DELETE FROM cultivations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
