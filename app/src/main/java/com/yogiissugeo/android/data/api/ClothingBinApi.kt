package com.yogiissugeo.android.data.api

import com.yogiissugeo.android.data.model.ClothingBinResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * 의류 수거함 위치 정보를 제공하는 API 인터페이스
 */
interface ClothingBinApi {
    /**
     * 의류 수거함 위치 목록을 동적으로 호출합니다.
     *
     * @param endpoint API 엔드포인트 경로
     * @param page 요청할 데이터의 페이지 번호 (1부터 시작)
     * @param perPage 한 페이지당 반환할 데이터 개수
     * @param serviceKey API 호출에 필요한 인증 키
     * @return ClothingBinResponse 객체를 포함하는 Response 객체
     */
    @GET
    suspend fun getClothingBinsByEndpoint(
        @Url endpoint: String,                 // 동적 엔드포인트 경로
        @Query("page") page: Int,              // 페이지 번호
        @Query("perPage") perPage: Int,        // 한 페이지당 반환할 데이터 개수
        @Query("serviceKey") serviceKey: String? // 인증에 사용되는 서비스 키
    ): Response<ClothingBinResponse>
}