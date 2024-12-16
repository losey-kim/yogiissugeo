package com.personal.yogiissugeo.data.repository

import com.personal.yogiissugeo.data.api.ClothingBinApi
import com.personal.yogiissugeo.data.api.GeocodingApi
import com.personal.yogiissugeo.data.model.ClothingBinResponse
import com.personal.yogiissugeo.data.model.GeocodingResponse
import com.personal.yogiissugeo.BuildConfig
import com.personal.yogiissugeo.data.api.GenericClothingBinApiHandler
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBin
import com.personal.yogiissugeo.utils.AddressCorrector
import com.personal.yogiissugeo.utils.safeApiCall
import retrofit2.Response
import javax.inject.Inject

/**
 * 의류 수거함 데이터를 관리하는 Repository 클래스
 *
 * @property clothingBinApi 의류 수거함 관련 API 호출을 처리하는 객체
 * @property geocodingApi 주소를 좌표로 변환하는 Geocoding API 호출을 처리하는 객체
 */
class ClothingBinRepository @Inject constructor(
    private val apiHandler: GenericClothingBinApiHandler,
    private val clothingBinApi: ClothingBinApi,    // 의류 수거함 API
    private val geocodingApi: GeocodingApi         // Geocoding API
) {

    /**
     * 특정 구의 의류 수거함 데이터를 가져옵니다.
     *
     * @param apiSource 호출할 구의 ApiSource
     * @param page 요청할 페이지 번호
     * @param perPage 한 페이지당 데이터 개수
     * @return ClothingBinResponse를 포함한 Result 객체
     */
    suspend fun getClothingBins(
        apiSource: ApiSource,
        page: Int,
        perPage: Int
    ): Result<ClothingBinResponse> {
        return safeApiCall {
            // API 호출: 특정 소스에서 데이터를 가져옴
            val response = apiHandler.fetchClothingBins(apiSource, page, perPage)

            // API 응답 본문(body) 추출
            val body = response.body()

            // 위도와 경도가 없는 데이터를 필터링
            val itemsWithoutCoordinates = body?.formattedData?.filter {
                it.latitude == null || it.longitude == null
            } ?: emptyList()

            // 필터링된 데이터에 대해 좌표 요청 및 병합
            val updatedItems = itemsWithoutCoordinates.mapNotNull { bin ->
                val correctedAddress = bin.address?.let { AddressCorrector.correct(it) } //주소 정제
                val geoResult = correctedAddress?.let { getCoordinates(correctedAddress) } // 좌표 요청

                // 주소 좌표 변환 결과 처리
                geoResult?.fold(
                    onSuccess = { geoData ->
                        val coordinates = geoData.addresses.firstOrNull() // 첫 번째 주소 좌표 가져오기
                        bin.copy(
                            address = correctedAddress, // 정제된 주소로 업데이트
                            latitude = coordinates?.y, //위도
                            longitude = coordinates?.x //경도
                        )
                    },
                    onFailure = { null } // 실패 시 null
                )
            }

            // 기존 데이터와 병합된 데이터를 결합하여 최종 리스트 생성
            val allBins = mergeBins(body?.formattedData ?: emptyList(), updatedItems)

            // 새로운 Response 생성
            Response.success(
                body?.copy(formattedData = allBins), // 변환된 데이터를 포함한 새로운 Body
                response.raw() // 기존 HTTP 응답 메타데이터 유지
            )
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
    private suspend fun getCoordinates(address: String): Result<GeocodingResponse> {
        return safeApiCall {
            geocodingApi.getCoordinates(
                address,
                BuildConfig.GEOCODING_API_KEY_ID,
                BuildConfig.GEOCODING_API_KEY
            )
        }
    }

    /**
     * 기존 데이터 리스트와 업데이트된 데이터를 병합하여 최종 리스트를 생성합니다.
     *
     * @param originalData 기존 의류 수거함 데이터 리스트.
     * @param updatedData 좌표 정보가 추가된 업데이트된 의류 수거함 데이터 리스트.
     * @return 병합된 의류 수거함 데이터 리스트.
     *         동일한 ID를 가진 항목은 updatedData의 데이터를 우선적으로 사용하며,
     *         업데이트된 데이터가 없는 항목은 originalData를 유지합니다.
     */
    private fun mergeBins(
        originalData: List<ClothingBin>,
        updatedData: List<ClothingBin>
    ): List<ClothingBin> {
        // updatedData를 ID를 키로 하는 Map으로 변환
        val updatedMap = updatedData.associateBy { it.id }

        // originalData를 순회하면서 updatedMap에 있는 경우 업데이트된 데이터를 사용
        return originalData.map { bin -> updatedMap[bin.id] ?: bin }
    }
}