package com.personal.yogiissugeo.data.api

import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBinResponse
import retrofit2.Response

/**
 * 의류 수거함 데이터를 가져오기 위한 API 호출을 처리하는 인터페이스
 */
interface ClothingBinApiHandler {
    /**
     * 지정된 API 소스에서 의류 수거함 데이터를 가져옵니다.
     *
     * @param apiSource 데이터를 가져올 API 소스를 나타냅니다.
     *                  (예: 서로 다른 API 또는 엔드포인트를 나타낼 수 있음)
     * @param page 요청할 페이지 번호를 나타냅니다. (페이징 처리에 사용)
     * @param perPage 페이지당 가져올 데이터의 개수를 설정합니다.
     * @return ClothingBinResponse 데이터를 포함하는 Response 객체를 반환하며,
     *         요청 실패 시 오류 정보를 포함합니다.
     */
    suspend fun fetchClothingBins(
        apiSource: ApiSource,
        page: Int,
        perPage: Int
    ): Response<ClothingBinResponse>
}