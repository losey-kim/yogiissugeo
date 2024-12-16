package com.personal.yogiissugeo.data.model

/**
 * 의류 수거함 데이터를 나타내는 데이터 클래스
 */
data class ClothingBin(
    val id: Int, // 고유 번호
    val address: String?, // 수거함 주소
    val latitude: String?, // 위도
    val longitude: String?, // 경도
    val administrativeDistrict: String?, //행정동
    val managingOrganization: String? //관리번호
)