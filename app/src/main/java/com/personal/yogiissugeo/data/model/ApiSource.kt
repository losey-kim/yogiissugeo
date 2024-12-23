package com.personal.yogiissugeo.data.model

import androidx.annotation.StringRes
import com.naver.maps.geometry.LatLng
import com.personal.yogiissugeo.R


/**
 * 의류 수거함 데이터를 제공하는 API 소스를 정의하는 Enum 클래스입니다.
 */
enum class ApiSource(
    val endpoint: String? = null,
    @StringRes val displayNameRes: Int,
    val csvName: String? = null,
    val defaultLatLng: LatLng
) {
    GANGNAM(
        endpoint = "15127131/v1/uddi:a9873b46-9551-407a-aff5-a3a77befb3d4",
        displayNameRes = R.string.gangnam,
        defaultLatLng = LatLng(37.5173050, 127.0475020)
    ),
    GANGDONG(
        endpoint = "15137988/v1/uddi:9bee791f-39d7-449d-aa52-bb400dc3d977",
        displayNameRes = R.string.gangdong,
        defaultLatLng = LatLng(37.5301260, 127.1237708)
    ),
    GANGBUK(
        endpoint = "15138051/v1/uddi:0561e1bd-cc51-43bb-8316-b399e6623f9b",
        displayNameRes = R.string.gangbuk,
        defaultLatLng = LatLng(37.6395417, 127.0254910)
    ),
    GANGSEO(
        endpoint = "15127065/v1/uddi:61d05f04-08d8-4e4f-ba17-8d6690775590",
        displayNameRes = R.string.gangseo,
        defaultLatLng = LatLng(37.5509370, 126.8496420)
    ),
    GWANAK(
        endpoint = "15076398/v1/uddi:6dec2a8d-6404-4318-8767-85419b3c45a0",
        displayNameRes = R.string.gwanak,
        defaultLatLng = LatLng(37.4781549, 126.9514847)
    ),
    GWANGJIN(
        endpoint = "15109594/v1/uddi:d63e68bf-e03d-4d3c-a203-fd9add3d372c",
        displayNameRes = R.string.gwangjin,
        defaultLatLng = LatLng(37.5386170, 127.0823750)
    ),
    GURO(
        endpoint = "15068871/v1/uddi:3fd97c80-6a9e-4d53-b65e-937d28de0605",
        displayNameRes = R.string.guro,
        defaultLatLng = LatLng(37.4954720, 126.8875360)
    ),
    GEUMCHEON(
        endpoint = "15106679/v1/uddi:2a54e58d-6b54-46de-9de1-cc3a6887ccb8",
        displayNameRes = R.string.geumcheon,
        defaultLatLng = LatLng(37.4568644, 126.8955105)
    ),
    DONGDAEMUN(
        endpoint = "15112228/v1/uddi:67d42349-302e-40f6-af11-c496e532d090",
        displayNameRes = R.string.dongdaemun,
        defaultLatLng = LatLng(37.5745240, 127.0396500)
    ),
    DONGJAK(
        endpoint = "15068021/v1/uddi:80e8a41d-469f-4556-b728-ce4fcf0c7f3b",
        displayNameRes = R.string.dongjak,
        defaultLatLng = LatLng(37.5124500, 126.9395000)
    ),
    MAPO(
        displayNameRes = R.string.mapo,
        csvName = "bin_list_mapo.csv",
        defaultLatLng = LatLng(37.5663245, 126.9014910)
    ),
    SEODAEMUN(
        endpoint = "15068863/v1/uddi:2682c872-adbe-4623-9e29-a53467734a88",
        displayNameRes = R.string.seodaemun,
        defaultLatLng = LatLng(37.5792250, 126.9368000)
    ),
    SEOCHO(
        endpoint = "15126956/v1/uddi:e1fc1767-5925-44f0-9eeb-ab332587885e",
        displayNameRes = R.string.seocho,
        defaultLatLng = LatLng(37.4835690, 127.0325980)
    ),
    SEONGDONG(
        endpoint = "15126958/v1/uddi:7d1e4696-cd38-4f0c-97c3-b93cc32af1fc",
        displayNameRes = R.string.seongdong,
        defaultLatLng = LatLng(37.5634560, 127.0368210)
    ),
    SEONGBUK(
        endpoint = "15127036/v1/uddi:f3c82d6f-498a-4e75-989b-e3fdc4720413",
        displayNameRes = R.string.seongbuk,
        defaultLatLng = LatLng(37.5894000, 127.0167490)
    ),
    SONGPA(
        endpoint = "15127100/v1/uddi:be5bca9a-0dbd-4a2a-b262-d5d7d8a6a4b0",
        displayNameRes = R.string.songpa,
        defaultLatLng = LatLng(37.5145636, 127.1059186)
    ),
    YANGCHEON(
        endpoint = "15105196/v1/uddi:3d00d6b8-e766-4b2e-990e-b6d310b9e792",
        displayNameRes = R.string.yangcheon,
        defaultLatLng = LatLng(37.5170160, 126.8666420)
    ),
    YEONGDEUNGPO(
        endpoint = "15106473/v1/uddi:a20df150-7ee3-4ca8-aa27-9b0f6d92d5c1",
        displayNameRes = R.string.yeongdeungpo,
        defaultLatLng = LatLng(37.5170160, 126.8666420)
    ),
    EUNPYEONG(
        displayNameRes = R.string.eunpyeong,
        csvName = "bin_list_eunpyeong.csv",
        defaultLatLng = LatLng(37.6027840, 126.9291640)
    ),
    JONGNO(
        endpoint = "15104622/v1/uddi:34ca4455-457d-4a50-ad1a-9b373f0f08eb",
        displayNameRes = R.string.jongno,
        defaultLatLng = LatLng(37.5735101, 126.9790062)
    ),
    JUNGNANG(
        endpoint = "15127304/v1/uddi:78d8746d-a497-4d27-9c0a-ddc69e71710f",
        displayNameRes = R.string.jungnang,
        defaultLatLng = LatLng(37.6063242, 127.0925842)
    );
}