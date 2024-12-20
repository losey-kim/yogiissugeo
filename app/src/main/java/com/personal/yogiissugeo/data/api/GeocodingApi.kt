package com.personal.yogiissugeo.data.api

import com.personal.yogiissugeo.data.model.GeocodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


/**
 * 네이버 지도 Geocoding API를 사용하여 주소를 좌표로 변환하는 인터페이스
 */
interface GeocodingApi {

    /**
     * 주소를 이용해 해당 위치의 좌표(위도, 경도)를 반환합니다.
     *
     * @param address 변환할 주소 문자열 (예: "서울특별시 구로구")
     * @param apiKeyId 네이버 클라우드 플랫폼 API Gateway의 인증 키 ID
     * @param apiKey 네이버 클라우드 플랫폼 API Gateway의 인증 키
     * @return GeocodingResponse 객체를 포함하는 Response 객체
     */
    @GET("map-geocode/v2/geocode")
    suspend fun getCoordinates(
        @Query("query") address: String,         // 변환할 주소
        @Header("x-ncp-apigw-api-key-id") apiKeyId: String?, // 네이버 API Gateway 인증 키 ID
        @Header("x-ncp-apigw-api-key") apiKey: String?       // 네이버 API Gateway 인증 키
    ): Response<GeocodingResponse>
}