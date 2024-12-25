package com.yogiissugeo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yogiissugeo.android.data.model.ClothingBin

@Dao
interface ClothingBinDao {
    /**
     * 특정 구의 데이터를 페이징 처리하여 가져옵니다.
     *
     * @param district 데이터가 속한 구 이름
     * @param limit 가져올 데이터의 최대 개수
     * @param offset 데이터의 시작 위치
     * @return 요청한 구의 의류 수거함 데이터 리스트
     */
    @Query("SELECT * FROM clothing_bin_table WHERE district = :district LIMIT :limit OFFSET :offset")
    suspend fun getBinsByDistrict(district: String, limit: Int, offset: Int): List<ClothingBin>

    /**
     * 특정 구에 저장된 데이터의 총 개수를 가져옵니다.
     *
     * @param district 데이터가 속한 구 이름
     * @return 저장된 데이터 개수
     */
    @Query("SELECT COUNT(*) FROM clothing_bin_table WHERE district = :district")
    suspend fun getStoredCountForDistrict(district: String): Int

    /**
     * 의류 수거함 데이터를 데이터베이스에 삽입합니다.
     * 중복 시 기존 데이터를 교체합니다.
     *
     * @param bins 삽입할 의류 수거함 데이터 리스트
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBins(bins: List<ClothingBin>)

    /**
     * 특정 구에 저장된 모든 데이터를 삭제합니다.
     *
     * @param district 삭제할 데이터가 속한 구 이름
     */
    @Query("DELETE FROM clothing_bin_table WHERE district = :district")
    suspend fun clearDistrictData(district: String)
}