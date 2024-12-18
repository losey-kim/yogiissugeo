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
            ApiSource.DONGJAK -> parseDongjak(data) // 동작구 데이터 파싱
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
            id = apiSource.name + (data["연번"] as? Double)?.toInt() , // "연번" 필드를 해당 구 명을 붙여 ID로 변환
            address = data["주소"] as? String, // "주소" 필드를 문자열로 변환
            administrativeDistrict = data["행정동"] as? String, // "행정동" 필드를 문자열로 변환
            district = apiSource.name //구로구 데이터 정보 추가
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
            id = apiSource.name + data["의류수거함"] as? String, // "의류수거함" 필드를 해당 구 명을 붙여 ID로 변환
            address = data["위치"] as? String, // "위치" 필드를 문자열로 변환
            latitude = data["위도"] as? String, // "위도" 필드의 값을 문자열로 변환
            longitude = data["경도"] as? String, // "경도" 필드의 값을 문자열로 변환
            district = apiSource.name //관악구 데이터 정보 추가
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
            id = apiSource.name + (data["연번"] as? Double)?.toInt(), // "연번" 필드를 해당 구 명을 붙여 ID로 변환
            address = data["설치장소(도로명)"] as? String, // "설치장소(도로명)" 필드를 문자열로 변환
            latitude = data["위도"] as? String, // "위도" 필드의 값을 문자열로 변환
            longitude = data["경도"] as? String, // "경도" 필드의 값을 문자열로 변환
            administrativeDistrict = data["행정동"] as? String, // "행정동" 필드를 문자열로 변환
            managingOrganization = data["관리단체"] as? String, // "관리단체" 필드를 문자열로 변환
            district = apiSource.name //서대문구 데이터 정보 추가
        )
    }
}