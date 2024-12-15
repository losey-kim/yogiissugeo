package com.personal.yogiissugeo.data.repository

import com.personal.yogiissugeo.data.api.ClothingBinApi
import com.personal.yogiissugeo.data.api.GeocodingApi
import com.personal.yogiissugeo.data.model.ClothingBinResponse
import com.personal.yogiissugeo.data.model.GeocodingResponse
import com.personal.yogiissugeo.BuildConfig
import com.personal.yogiissugeo.utils.safeApiCall
import javax.inject.Inject

/**
 * 의류 수거함 데이터를 관리하는 Repository 클래스
 *
 * @property clothingBinApi 의류 수거함 관련 API 호출을 처리하는 객체
 * @property geocodingApi 주소를 좌표로 변환하는 Geocoding API 호출을 처리하는 객체
 */
class ClothingBinRepository @Inject constructor(
    private val clothingBinApi: ClothingBinApi,    // 의류 수거함 API
    private val geocodingApi: GeocodingApi         // Geocoding API
) {
    /**
     * 구로구 의류 수거함 위치 목록을 가져옵니다.
     *
     * @param page 요청할 페이지 번호
     * @param perPage 한 페이지당 반환할 항목 수
     * @return ClothingBinResponse 객체를 포함하는 Result 객체
     *
     * - 내부적으로 safeApiCall을 사용하여 API 요청 중 발생할 수 있는 예외를 처리합니다.
     */
    suspend fun getClothingBins(page: Int, perPage: Int): Result<ClothingBinResponse> {
        return safeApiCall {
            clothingBinApi.getClothingBins(page, perPage, BuildConfig.CLOTHING_BIN_API_KEY)
        }
    }

    /**
     * 주소를 사용하여 해당 위치의 좌표(위도, 경도)를 가져옵니다.
     *
     * @param address 변환할 주소 문자열
     * @return GeocodingResponse 객체를 포함하는 Result 객체
     *
     * - 내부적으로 safeApiCall을 사용하여 API 요청 중 발생할 수 있는 예외를 처리합니다.
     */
    suspend fun getCoordinates(address: String): Result<GeocodingResponse> {
        return safeApiCall {
            geocodingApi.getCoordinates(address, BuildConfig.GEOCODING_API_KEY_ID, BuildConfig.GEOCODING_API_KEY)
        }
    }
}