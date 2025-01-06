package com.yogiissugeo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Remote Config에서 가져온 재활용 정보를 나타내는 데이터 클래스
 */
data class RecyclingInfoResponse(
    @SerializedName("recycling_info")
    val recyclingInfo: List<RecyclingInfo>
)

/**
 * 특정 구의 재활용 정보를 나타내는 데이터 클래스
 */
data class RecyclingInfo(
    val district: String,
    val department: String,
    val url: String
)