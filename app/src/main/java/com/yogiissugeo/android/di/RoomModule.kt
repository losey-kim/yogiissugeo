package com.yogiissugeo.android.di

import android.content.Context
import androidx.room.Room
import com.yogiissugeo.android.data.local.dao.ClothingBinDao
import com.yogiissugeo.android.data.local.dao.DistrictDataCountDao
import com.yogiissugeo.android.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room 데이터베이스와 DAO 관련 의존성들을 제공하는 모듈입니다.
 * Room 데이터베이스와 관련 DAO를 제공
 */
@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    /**
     * Room 데이터베이스를 생성하여 애플리케이션 범위(Singleton)로 제공합니다.
     *
     * @param context 애플리케이션 Context
     * @return Room 데이터베이스의 AppDatabase 인스턴스
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "clothing_bin_db"
        ).build()
    }

    /**
     * 의류 수거함 DAO를 제공
     *
     * @param database AppDatabase 인스턴스
     * @return ClothingBinDao 인스턴스
     */
    @Provides
    fun provideBinDao(database: AppDatabase): ClothingBinDao {
        return database.clothingBinDao()
    }

    /**
     * 구 데이터 카운트 관리 DAO를 제공
     *
     * @param database AppDatabase 인스턴스
     * @return DistrictDataCountDao 인스턴스
     */
    @Provides
    fun provideDistrictDataCountDao(database: AppDatabase): DistrictDataCountDao {
        return database.districtDataCountDao()
    }
}