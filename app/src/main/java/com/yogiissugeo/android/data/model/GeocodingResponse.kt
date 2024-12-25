package com.yogiissugeo.android.data.model

/**
 * 주소를 좌표로 변환하는 Geocoding API 응답 데이터를 나타내는 데이터 클래스
 */
data class GeocodingResponse(
    val status: String,            // 요청 상태 (예: "OK")
    val meta: Meta,                // 메타데이터
    val addresses: List<Address>,  // 주소 리스트
    val errorMessage: String?      // 에러 메시지 (있을 경우)
)

/**
 * Geocoding API의 메타데이터를 나타내는 데이터 클래스
 */
data class Meta(
    val totalCount: Int,  // 총 결과 개수
    val page: Int,        // 현재 페이지
    val count: Int        // 현재 페이지의 결과 개수
)

/**
 * Geocoding API의 주소 데이터를 나타내는 데이터 클래스
 */
data class Address(
    val roadAddress: String?,      // 도로명 주소
    val jibunAddress: String?,     // 지번 주소
    val englishAddress: String?,   // 영문 주소
    val addressElements: List<AddressElement>, // 주소 요소 리스트
    val x: String,                 // 경도 (Longitude)
    val y: String,                 // 위도 (Latitude)
    val distance: Double           // 거리 (기본값: 0.0)
)

/**
 * Geocoding API에서 반환된 주소 요소를 나타내는 데이터 클래스
 */
data class AddressElement(
    val types: List<String>,       // 주소 타입 (예: "SIDO", "ROAD_NAME" 등)
    val longName: String,          // 전체 이름
    val shortName: String,         // 간략 이름
    val code: String?              // 코드 (있을 경우)
)