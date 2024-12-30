package com.yogiissugeo.android.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 특정 구의 데이터 개수를 관리하는 엔티티 클래스
 *
 * @property district 구 이름
 * @property totalCount 해당 구에 저장된 총 데이터 개수
 */
@Entity(tableName = "district_data_count")
data class DistrictDataCount(
    @PrimaryKey val district: String, // 구 이름
    val totalCount: Int               // 해당 구의 총 데이터 수
)