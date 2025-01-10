package com.yogiissugeo.android.data.model

import androidx.annotation.StringRes
import com.yogiissugeo.android.R


/**
 * 의류 수거함 데이터를 제공하는 API 소스를 정의하는 Enum 클래스입니다.
 */
enum class ApiSource(
    val csvName: String? = null,
    val endpoint: String? = null,
    @StringRes val displayNameRes: Int
) {
    GANGNAM(
        endpoint = "15127131/v1/uddi:a9873b46-9551-407a-aff5-a3a77befb3d4",
        displayNameRes = R.string.gangnam,
    ),
    GANGDONG(
        endpoint = "15137988/v1/uddi:9bee791f-39d7-449d-aa52-bb400dc3d977",
        displayNameRes = R.string.gangdong,
    ),
    GANGBUK(
        endpoint = "15138051/v1/uddi:0561e1bd-cc51-43bb-8316-b399e6623f9b",
        displayNameRes = R.string.gangbuk,
    ),
    GANGSEO(
        endpoint = "15127065/v1/uddi:61d05f04-08d8-4e4f-ba17-8d6690775590",
        displayNameRes = R.string.gangseo,
    ),
    GWANAK(
        endpoint = "15076398/v1/uddi:6dec2a8d-6404-4318-8767-85419b3c45a0",
        displayNameRes = R.string.gwanak,
    ),
    GWANGJIN(
        endpoint = "15109594/v1/uddi:d63e68bf-e03d-4d3c-a203-fd9add3d372c",
        displayNameRes = R.string.gwangjin,
    ),
    GURO(
        endpoint = "15068871/v1/uddi:3fd97c80-6a9e-4d53-b65e-937d28de0605",
        displayNameRes = R.string.guro,
    ),
    GEUMCHEON(
        endpoint = "15106679/v1/uddi:2a54e58d-6b54-46de-9de1-cc3a6887ccb8",
        displayNameRes = R.string.geumcheon,
    ),
    NOWON(
        csvName = "bin_list_nowon.csv",
        displayNameRes = R.string.nowon,
    ),
    DONGDAEMUN(
        endpoint = "15112228/v1/uddi:67d42349-302e-40f6-af11-c496e532d090",
        displayNameRes = R.string.dongdaemun,
    ),
    DONGJAK(
        endpoint = "15068021/v1/uddi:80e8a41d-469f-4556-b728-ce4fcf0c7f3b",
        displayNameRes = R.string.dongjak,
    ),
    MAPO(
        csvName = "bin_list_mapo.csv",
        displayNameRes = R.string.mapo,
    ),
    SEODAEMUN(
        endpoint = "15068863/v1/uddi:2682c872-adbe-4623-9e29-a53467734a88",
        displayNameRes = R.string.seodaemun,
    ),
    SEOCHO(
        endpoint = "15126956/v1/uddi:e1fc1767-5925-44f0-9eeb-ab332587885e",
        displayNameRes = R.string.seocho,
    ),
    SEONGDONG(
        endpoint = "15126958/v1/uddi:7d1e4696-cd38-4f0c-97c3-b93cc32af1fc",
        displayNameRes = R.string.seongdong,
    ),
    SEONGBUK(
        endpoint = "15127036/v1/uddi:f3c82d6f-498a-4e75-989b-e3fdc4720413",
        displayNameRes = R.string.seongbuk,
    ),
    SONGPA(
        endpoint = "15127100/v1/uddi:be5bca9a-0dbd-4a2a-b262-d5d7d8a6a4b0",
        displayNameRes = R.string.songpa,
    ),
    YANGCHEON(
        endpoint = "15105196/v1/uddi:3d00d6b8-e766-4b2e-990e-b6d310b9e792",
        displayNameRes = R.string.yangcheon,
    ),
    YEONGDEUNGPO(
        endpoint = "15106473/v1/uddi:a20df150-7ee3-4ca8-aa27-9b0f6d92d5c1",
        displayNameRes = R.string.yeongdeungpo,
    ),
    EUNPYEONG(
        csvName = "bin_list_eunpyeong.csv",
        displayNameRes = R.string.eunpyeong,
    ),
    JONGNO(
        endpoint = "15104622/v1/uddi:34ca4455-457d-4a50-ad1a-9b373f0f08eb",
        displayNameRes = R.string.jongno,
    ),
    JUNGGU(
        csvName = "bin_list_junggu.csv",
        displayNameRes = R.string.junggu,
    ),
    JUNGNANG(
        endpoint = "15127304/v1/uddi:78d8746d-a497-4d27-9c0a-ddc69e71710f",
        displayNameRes = R.string.jungnang,
    );

    // csvName이 null이 아니면 CSV 소스임
    val isCsvSource: Boolean
        get() = csvName != null

    companion object {
        //csv파일 파서
        val csvParsers: Map<ApiSource, (Int, Array<String>) -> ClothingBin?> = mapOf(
            //노원구
            NOWON to { _, row ->
                if (row[3].isNotEmpty() && row[4].isNotEmpty()) {
                    ClothingBin(
                        id = NOWON.name + row[0],
                        address = row[2],
                        latitude = row[3],
                        longitude = row[4],
                        district = NOWON.name
                    )
                } else null
            },
            //마포구
            MAPO to { _, row ->
                if (row[2].isNotEmpty() && row[3].isNotEmpty()) {
                    ClothingBin(
                        id = MAPO.name + row[0],
                        address = row[1],
                        latitude = row[2],
                        longitude = row[3],
                        district = MAPO.name
                    )
                } else null
            },
            //은평구
            EUNPYEONG to { index, row ->
                if (row[3].isNotEmpty() && row[4].isNotEmpty()) {
                    ClothingBin(
                        id = EUNPYEONG.name + index.toString(),
                        address = row[2],
                        latitude = row[4],
                        longitude = row[3],
                        district = EUNPYEONG.name
                    )
                } else null
            },
            //중구
            JUNGGU to { _, row ->
                if (row[6].isNotEmpty() && row[7].isNotEmpty()) {
                    ClothingBin(
                        id = JUNGGU.name + row[0],
                        address = row[4],
                        latitude = row[6],
                        longitude = row[7],
                        district = JUNGGU.name
                    )
                } else null
            }
        )
    }
}