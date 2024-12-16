package com.personal.yogiissugeo.data.parser

import com.personal.yogiissugeo.data.model.ClothingBin

/**
 * 의류 수거함 데이터를 파싱하기 위한 인터페이스입니다.
 * API로부터 가져온 데이터를 특정 형식으로 변환하는 역할을 합니다.
 */
interface ClothingBinParser {
    /**
     * API 응답 데이터를 ClothingBin 객체로 변환합니다.
     *
     * @param data API로부터 전달된 데이터를 나타내는 Map.
     *             키는 데이터의 필드 이름이고 값은 해당 필드의 실제 값입니다.
     * @return 변환된 ClothingBin 객체.
     */
    fun parse(data: Map<String, Any>): ClothingBin
}