package com.yogiissugeo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yogiissugeo.android.data.local.model.DistrictDataCount

@Dao
interface DistrictDataCountDao {
    /**
     * 특정 구의 총 데이터 개수를 가져옵니다.(저장된 데이터 개수 아님!!)
     *
     * @param district 데이터가 속한 구 이름
     * @return 해당 구의 총 데이터 개수, 데이터가 없으면 null 반환
     */
    @Query("SELECT totalCount FROM district_data_count WHERE district = :district")
    suspend fun getTotalCount(district: String): Int?

    /**
     * 특정 구의 총 데이터 개수를 저장하거나 기존 데이터를 업데이트합니다.(저장된 데이터 개수 아님!!)
     * 중복 시 기존 데이터를 교체
     *
     * @param dataCount 저장 또는 업데이트할 구의 데이터 개수 정보
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCount(dataCount: DistrictDataCount)

    /**
     * districtName과 count만 받아서 entity로 변환하는 함수
     */
    @Transaction
    suspend fun insertOrUpdateCount(districtName: String, totalCount: Int) {
        insertOrUpdateCount(DistrictDataCount(districtName, totalCount))
    }
}