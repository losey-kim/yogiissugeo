package com.yogiissugeo.android.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * RemoteConfig에서 지원되는 District 데이터를 나타내는 데이터 클래스
 * @property supportedDistricts 지원되는 District의 이름 목록
 */
@Keep
data class SupportedDistricts(
    @SerializedName("supported_districts")
    val supportedDistricts: List<String>
)