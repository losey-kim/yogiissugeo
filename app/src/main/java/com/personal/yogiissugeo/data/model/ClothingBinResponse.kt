package com.personal.yogiissugeo.data.model

/**
 * 의류 수거함 API 응답 데이터를 나타내는 데이터 클래스
 */
data class ClothingBinResponse(
    val currentCount: Int,       // 현재 페이지의 항목 수
    val data: List<Map<String, Any>>, // 원본 의류 수거함 데이터 리스트
    val formattedData: List<ClothingBin>? = null, // 변환된 의류 수거함 데이터 리스트
    val matchCount: Int,         // 조건에 맞는 전체 데이터 수
    val page: Int,               // 현재 페이지 번호
    val perPage: Int,            // 페이지당 항목 수
    val totalCount: Int          // 전체 데이터 수
)