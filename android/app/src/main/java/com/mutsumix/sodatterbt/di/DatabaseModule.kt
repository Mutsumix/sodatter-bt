package com.mutsumix.sodatterbt.di

import android.content.Context
import androidx.room.Room
import com.mutsumix.sodatterbt.data.db.SodatterDatabase
import com.mutsumix.sodatterbt.data.db.dao.CultivationDao
import com.mutsumix.sodatterbt.data.db.dao.DeviceDao
import com.mutsumix.sodatterbt.data.db.dao.DeviceSettingDao
import com.mutsumix.sodatterbt.data.db.dao.GrowthPhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SodatterDatabase =
        Room.databaseBuilder(context, SodatterDatabase::class.java, "sodatter.db")
            .addCallback(SodatterDatabase.SeedCallback())
            .build()

    @Provides
    fun provideDeviceDao(db: SodatterDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideCultivationDao(db: SodatterDatabase): CultivationDao = db.cultivationDao()

    @Provides
    fun provideGrowthPhotoDao(db: SodatterDatabase): GrowthPhotoDao = db.growthPhotoDao()

    @Provides
    fun provideDeviceSettingDao(db: SodatterDatabase): DeviceSettingDao = db.deviceSettingDao()
}
