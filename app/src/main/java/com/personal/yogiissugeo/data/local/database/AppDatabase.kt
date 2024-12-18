package com.personal.yogiissugeo.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.personal.yogiissugeo.data.local.dao.ClothingBinDao
import com.personal.yogiissugeo.data.local.dao.DistrictDataCountDao
import com.personal.yogiissugeo.data.model.ClothingBin
import com.personal.yogiissugeo.data.model.DistrictDataCount

/**
 * 앱의 데이터베이스를 정의하는 RoomDatabase 클래스
 *
 * @entities 데이터베이스에서 관리하는 엔티티 클래스 목록
 * - ClothingBin: 의류 수거함 정보를 나타내는 엔티티
 * - DistrictDataCount: 특정 구의 데이터 총 개수를 관리하는 엔티티(저장된 데이터 개수 아님!!)
 * @version 데이터베이스 버전
 * @exportSchema 스키마 파일을 export할지 여부
 */
@Database(entities = [ClothingBin::class, DistrictDataCount::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun districtDataCountDao(): DistrictDataCountDao
    abstract fun clothingBinDao(): ClothingBinDao
}