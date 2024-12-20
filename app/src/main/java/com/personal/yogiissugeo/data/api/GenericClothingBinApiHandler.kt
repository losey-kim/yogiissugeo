package com.personal.yogiissugeo.data.api

import com.personal.yogiissugeo.BuildConfig
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBinResponse
import com.personal.yogiissugeo.data.parser.ClothingBinParser
import retrofit2.Response

/**
 * GenericClothingBinApiHandler는 ClothingBinApiHandler를 구현하여
 * 특정 API 소스에서 의류 수거함 데이터를 가져오고 가공하는 역할을 합니다.
 *
 * @param clothingBinApi 의류 수거함 데이터를 요청하는 Retrofit API 인터페이스
 */
class GenericClothingBinApiHandler(
    private val clothingBinApi: ClothingBinApi
) : ClothingBinApiHandler {

    /**
     * API 호출을 통해 의류 수거함 데이터를 가져오고 변환합니다.
     *
     * @param apiSource API 소스 정보 (엔드포인트 등)
     * @param page 요청할 페이지 번호
     * @param perPage 페이지당 데이터 개수
     * @return ClothingBinResponse 객체를 포함하는 Response
     */
    override suspend fun fetchClothingBins(
        apiSource: ApiSource,
        page: Int,
        perPage: Int
    ): Response<ClothingBinResponse> {
        // API 호출: 특정 엔드포인트와 페이지 정보를 사용하여 데이터 요청
        val response = clothingBinApi.getClothingBinsByEndpoint(
            endpoint = apiSource.endpoint,
            page = page,
            perPage = perPage,
            serviceKey = BuildConfig.CLOTHING_BIN_API_KEY
        )

        // API 호출 성공 여부 확인
        if (response.isSuccessful) {
            // 응답 본문(body)이 null인 경우 safeApiCall에서 처리하므로 그대로 반환
            val body = response.body() ?: return response

            // API 소스에 맞는 Parser 생성
            val parser = ClothingBinParser(apiSource)

            // 응답 데이터를 지정된 Parser를 사용하여 변환
            val transformedData = body.data.map { parser.parse(it) }

            // 변환된 데이터를 포함하는 새로운 Response 객체 생성 및 반환
            return Response.success(
                body.copy(formattedData = transformedData),
                response.raw()
            )
        } else {
            // 실패 응답을 그대로 반환
            return response
        }
    }
}