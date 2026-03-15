package com.mutsumix.sodatterbt.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mutsumix.sodatterbt.data.db.dao.CultivationDao
import com.mutsumix.sodatterbt.data.db.dao.DeviceDao
import com.mutsumix.sodatterbt.data.db.dao.DeviceSettingDao
import com.mutsumix.sodatterbt.data.db.dao.GrowthPhotoDao
import com.mutsumix.sodatterbt.data.db.entity.CultivationEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceEntity
import com.mutsumix.sodatterbt.data.db.entity.DeviceSettingEntity
import com.mutsumix.sodatterbt.data.db.entity.GrowthPhotoEntity

@Database(
    entities = [
        DeviceEntity::class,
        CultivationEntity::class,
        GrowthPhotoEntity::class,
        DeviceSettingEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SodatterDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun cultivationDao(): CultivationDao
    abstract fun growthPhotoDao(): GrowthPhotoDao
    abstract fun deviceSettingDao(): DeviceSettingDao

    class SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // 装置4台をシードデータとして挿入
            db.execSQL(
                "INSERT INTO devices (id, name) VALUES (1, 'A'), (2, 'B'), (3, 'C'), (4, 'D')"
            )
        }
    }
}
