package com.personal.yogiissugeo.data.model

import com.google.gson.annotations.SerializedName

/**
 * 의류 수거함 데이터를 나타내는 데이터 클래스
 */
data class ClothingBin(
    @SerializedName("데이터기준일자") val dataDate: String,  // 데이터 기준일
    @SerializedName("연번") val sequence: Int,           // 고유 번호
    @SerializedName("주소") val address: String,          // 수거함 주소
    @SerializedName("행정동") val district: String,        // 행정동 이름
    val latitude: String? = null,                        // 위도
    val longitude: String? = null                        // 경도
)