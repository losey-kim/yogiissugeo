package com.personal.yogiissugeo.data.parser

import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBin

/**
 * GenericClothingBinParser는 API 소스에 따라 데이터를 파싱하여 ClothingBin 객체로 변환하는 클래스입니다.
 *
 * @param apiSource 데이터를 파싱할 API 소스를 나타냅니다.
 */
class GenericClothingBinParser(private val apiSource: ApiSource) : ClothingBinParser {

    /**
     * API 소스에 따라 적절한 파싱 로직을 호출합니다.
     *
     * @param data API 응답 데이터. 키는 데이터 필드 이름, 값은 해당 필드의 실제 값입니다.
     * @return 변환된 ClothingBin 객체.
     */
    override fun parse(data: Map<String, Any>): ClothingBin {
        return when (apiSource) {
            ApiSource.GURO -> parseGuro(data) // 구로구 데이터 파싱
            ApiSource.GWANAK -> parseGwanak(data) // 관악구 데이터 파싱
            ApiSource.SEODAEMUN -> parseSeodaemun(data) // 서대문구 데이터 파싱
            else -> throw IllegalArgumentException("지원하지 않는 데이터: $apiSource")
        }
    }

    /**
     * 구로구 데이터를 ClothingBin 객체로 변환합니다.
     *
     * @param data API 응답 데이터.
     * @return 변환된 ClothingBin 객체.
     */
    private fun parseGuro(data: Map<String, Any>): ClothingBin {
        return ClothingBin(
            id = data["연번"] as? Int ?: 0, // "연번" 필드를 정수 ID로 변환, 없으면 기본값 0
            address = data["주소"] as? String, // "주소" 필드를 문자열로 변환
            latitude = null, // 구로구 데이터에는 위도 정보 없음
            longitude = null, // 구로구 데이터에는 경도 정보 없음
            administrativeDistrict = data["행정동"] as? String, // "행정동" 필드를 문자열로 변환
            managingOrganization = null // 구로구 데이터에는 관리 기관 정보 없음
        )
    }

    /**
     * 관악구 데이터를 ClothingBin 객체로 변환합니다.
     *
     * @param data API 응답 데이터.
     * @return 변환된 ClothingBin 객체.
     */
    private fun parseGwanak(data: Map<String, Any>): ClothingBin {
        return ClothingBin(
            id = (data["의류수거함"] as? String)?.split("-")?.last()?.toIntOrNull() ?: 0, // "의류수거함" 필드에서 ID 추출
            address = data["위치"] as? String, // "위치" 필드를 문자열로 변환
            latitude = data["위도"] as? String, // "위도" 필드의 값을 문자열로 변환
            longitude = data["경도"] as? String, // "경도" 필드의 값을 문자열로 변환
            administrativeDistrict = null, // 관악구 데이터에는 행정구역 정보 없음
            managingOrganization = null // 관악구 데이터에는 관리 기관 정보 없음
        )
    }

    /**
     * 서대문구 데이터를 ClothingBin 객체로 변환합니다.
     *
     * @param data API 응답 데이터.
     * @return 변환된 ClothingBin 객체.
     */
    private fun parseSeodaemun(data: Map<String, Any>): ClothingBin {
        return ClothingBin(
            id = data["연번"] as? Int ?: 0, // "연번" 필드를 정수 ID로 변환, 없으면 기본값 0
            address = data["설치장소(도로명)"] as? String, // "설치장소(도로명)" 필드를 문자열로 변환
            latitude = data["위도"] as? String, // "위도" 필드의 값을 문자열로 변환
            longitude = data["경도"] as? String, // "경도" 필드의 값을 문자열로 변환
            administrativeDistrict = data["행정동"] as? String, // "행정동" 필드를 문자열로 변환
            managingOrganization = data["관리단체"] as? String // "관리단체" 필드를 문자열로 변환
        )
    }
}