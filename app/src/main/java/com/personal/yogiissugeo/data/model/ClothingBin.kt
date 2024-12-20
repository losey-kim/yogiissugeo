package com.personal.yogiissugeo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 의류 수거함 데이터를 나타내는 데이터 클래스
 * Room의 엔티티 및 API 데이터를 받아오는 데도 사용
 */
@Entity(tableName = "clothing_bin_table")
data class ClothingBin(
    @PrimaryKey val id: String, // 고유 번호
    val address: String? = null, // 수거함 주소
    val latitude: String? = null, // 위도
    val longitude: String? = null, // 경도
    val district: String? = null //구 이름
)