package com.personal.yogiissugeo.data.model

import androidx.annotation.StringRes
import com.personal.yogiissugeo.R

/**
 * 의류 수거함 데이터를 제공하는 API 소스를 정의하는 Enum 클래스입니다.
 *
 * @property endpoint API 호출에 사용되는 엔드포인트 URL
 * @property displayNameRes 사용자에게 표시할 소스 이름의 리소스 ID
 */
enum class ApiSource(val endpoint: String, @StringRes val displayNameRes: Int) {
    GURO("15068871/v1/uddi:3fd97c80-6a9e-4d53-b65e-937d28de0605", R.string.guro),
    GWANAK("15076398/v1/uddi:6dec2a8d-6404-4318-8767-85419b3c45a0", R.string.gwanak),
    SEODAEMUN("15068863/v1/uddi:2682c872-adbe-4623-9e29-a53467734a88", R.string.seodaemun);
}