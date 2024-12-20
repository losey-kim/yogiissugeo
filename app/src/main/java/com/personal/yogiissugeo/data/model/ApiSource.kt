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
    GANGNAM("15127131/v1/uddi:a9873b46-9551-407a-aff5-a3a77befb3d4", R.string.gangnam),
    GANGDONG("15137988/v1/uddi:9bee791f-39d7-449d-aa52-bb400dc3d977", R.string.gangdong),
    GANGBUK("15138051/v1/uddi:0561e1bd-cc51-43bb-8316-b399e6623f9b", R.string.gangbuk),
    GANGSEO("15127065/v1/uddi:61d05f04-08d8-4e4f-ba17-8d6690775590", R.string.gangseo),
    GWANAK("15076398/v1/uddi:6dec2a8d-6404-4318-8767-85419b3c45a0", R.string.gwanak),
    GWANGJIN("15109594/v1/uddi:d63e68bf-e03d-4d3c-a203-fd9add3d372c", R.string.gwangjin),
    GURO("15068871/v1/uddi:3fd97c80-6a9e-4d53-b65e-937d28de0605", R.string.guro),
    GEUMCHEON("15106679/v1/uddi:2a54e58d-6b54-46de-9de1-cc3a6887ccb8", R.string.geumcheon),
    DONGDAEMUN("15112228/v1/uddi:67d42349-302e-40f6-af11-c496e532d090", R.string.dongdaemun),
    DONGJAK("15068021/v1/uddi:80e8a41d-469f-4556-b728-ce4fcf0c7f3b", R.string.dongjak),
    SEODAEMUN("15068863/v1/uddi:2682c872-adbe-4623-9e29-a53467734a88", R.string.seodaemun),
    SEOCHO("15126956/v1/uddi:e1fc1767-5925-44f0-9eeb-ab332587885e", R.string.seocho),
    SEONGDONG("15126958/v1/uddi:7d1e4696-cd38-4f0c-97c3-b93cc32af1fc", R.string.seongdong),
    SEONGBUK("15127036/v1/uddi:f3c82d6f-498a-4e75-989b-e3fdc4720413", R.string.seongbuk),
    SONGPA("15127100/v1/uddi:be5bca9a-0dbd-4a2a-b262-d5d7d8a6a4b0", R.string.songpa),
    YANGCHEON("15105196/v1/uddi:3d00d6b8-e766-4b2e-990e-b6d310b9e792", R.string.yangcheon),
    YEONGDEUNGPO("15106473/v1/uddi:a20df150-7ee3-4ca8-aa27-9b0f6d92d5c1", R.string.yeongdeungpo),
    JONGNO("15104622/v1/uddi:34ca4455-457d-4a50-ad1a-9b373f0f08eb", R.string.jongno),
    JUNGNANG("15127304/v1/uddi:78d8746d-a497-4d27-9c0a-ddc69e71710f", R.string.jungnang);
}