package com.personal.yogiissugeo.data.parser

import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBin

/**
 * API 소스에 따라 데이터를 파싱하여 ClothingBin 객체로 변환하는 클래스입니다.
 *
 * @param apiSource 데이터를 파싱할 API 소스를 나타냅니다.
 */
class ClothingBinParser(private val apiSource: ApiSource) {

    // 각 ApiSource에 대한 개별 키 매핑 정의
    private val keyMappings = mapOf(
        ApiSource.GANGNAM to KeyMapping("연번", "도로명 주소", "위도", "경도"),
        ApiSource.GANGDONG to KeyMapping("연번", "도로명 주소", "Y좌표", "X좌표"),
        ApiSource.GANGBUK to KeyMapping("의류수거함관리코드", "도로명주소", "위도", "경도"),
        ApiSource.GANGSEO to KeyMapping("관리번호", "설치장소(도로명주소)", "위도", "경도"),
        ApiSource.GWANAK to KeyMapping("의류수거함", "위치", "위도", "경도"),
        ApiSource.GWANGJIN to KeyMapping("지번주소", "도로명주소", "위도", "경도"),
        ApiSource.GURO to KeyMapping("연번", "주소", null, null),
        ApiSource.DONGJAK to KeyMapping("연번", "주소", "위도", "경도"),
        ApiSource.SEODAEMUN to KeyMapping("관리번호", "설치장소(도로명)", "위도", "경도"),
        ApiSource.SEONGDONG to KeyMapping("순번", "설치장소", "위도", "경도"),
        ApiSource.SEONGBUK to KeyMapping("연번", "도로명주소", "위도", "경도"),
        ApiSource.SONGPA to KeyMapping("연번", "설치장소", "위도", "경도")
    )

    // 동일한 키 매핑을 사용하는 ApiSource 그룹 정의
    private val sharedGroups = mapOf(
        setOf(
            ApiSource.GWANGJIN, ApiSource.GEUMCHEON, ApiSource.SEOCHO,
            ApiSource.YANGCHEON, ApiSource.YEONGDEUNGPO, ApiSource.JONGNO // 광진구와 동일한 키 매핑
        ) to KeyMapping("지번주소", "도로명주소", "위도", "경도"),
        setOf(
            ApiSource.SEONGBUK, ApiSource.JUNGNANG // 성북구와 동일한 키 매핑
        ) to KeyMapping("연번", "도로명주소", "위도", "경도"),
        setOf(
            ApiSource.DONGDAEMUN, ApiSource.DONGJAK // 동대문구와 동일한 키 매핑
        ) to KeyMapping("연번", "주소", "위도", "경도")
    )

    /**
     * 주어진 ApiSource에 따라 적절한 ClothingBin 객체를 생성합니다.
     *
     * @param data API 응답 데이터 (Map 형태로 제공)
     * @return 변환된 ClothingBin 객체
     */
    fun parse(data: Map<String, Any>): ClothingBin {
        // ApiSource에 해당하는 키 매핑 검색
        val mapping = findKeyMapping(apiSource) ?: error("Unsupported ApiSource: $apiSource")

        // idKey 값의 타입에 따라 분기 처리
        val id = when (val idKeyValue = data[mapping.idKey]) {
            is Double -> "${apiSource.name}${idKeyValue.toInt()}" // Double 타입이면 정수로 변환
            is String -> "${apiSource.name}$idKeyValue" // String 타입이면 그대로 사용
            else -> apiSource.name // 예상치 못한 타입 처리
        }

        // 키 매핑에 따라 ClothingBin 객체 생성
        return ClothingBin(
            id = id,
            address = data[mapping.addressKey] as? String,
            latitude = data[mapping.latitudeKey] as? String,
            longitude = data[mapping.longitudeKey] as? String,
            district = apiSource.name
        )
    }

    // 키 매핑 검색
    private fun findKeyMapping(apiSource: ApiSource): KeyMapping? {
        // 공통 그룹에서 매핑 검색
        for ((group, mapping) in sharedGroups) {
            if (apiSource in group) return mapping
        }
        // 개별 매핑에서 검색
        return keyMappings[apiSource]
    }

    // 키 매핑 클래스
    data class KeyMapping(
        val idKey: String,       // ID 생성에 사용하는 키
        val addressKey: String,  // 주소 필드 키
        val latitudeKey: String?, // 위도 필드 키 (nullable)
        val longitudeKey: String? // 경도 필드 키 (nullable)
    )
}