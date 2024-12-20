package com.personal.yogiissugeo.di

import com.personal.yogiissugeo.BuildConfig
import com.personal.yogiissugeo.data.api.ClothingBinApi
import com.personal.yogiissugeo.data.api.ClothingBinApiHandler
import com.personal.yogiissugeo.data.api.GeocodingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성들을 제공하는 모듈입니다.
 * Retrofit, OkHttpClient 및 관련 API 인스턴스를 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class) // SingletonComponent에 의존성 주입을 설치
object NetworkModule {

    /**
     * OkHttpClient 인스턴스를 제공합니다.
     * 네트워크 요청을 처리하는 OkHttp 클라이언트를 설정하고 반환합니다.
     *
     * @return OkHttpClient 인스턴스
     */
    @Provides
    @Singleton // 싱글톤으로 제공하여 애플리케이션 전체에서 동일한 인스턴스를 사용
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 디버그 모드에서만 로그 레벨을 BODY로 설정하여 요청과 응답의 전체 내용을 확인할 수 있도록 함
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // OkHttpClient를 생성하고, 로깅 인터셉터를 추가하여 반환
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
            .build()
    }

    /**
     * ClothingBin API와 통신하기 위한 Retrofit 인스턴스를 제공합니다.
     *
     * @param okHttpClient OkHttpClient 인스턴스
     * @return ClothingBinApi와 연결된 Retrofit 인스턴스
     */
    @ClothingBinRetrofit // ClothingBinRetrofit 애노테이션을 사용하여 특정 Retrofit 인스턴스를 구별
    @Provides
    @Singleton
    fun provideClothingBinRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.odcloud.kr/api/") // ClothingBinApi의 BaseUrl
            .client(okHttpClient) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create()) // Gson을 이용한 응답 변환기 설정
            .build() // Retrofit 인스턴스 생성
    }

    /**
     * Geocoding API와 통신하기 위한 Retrofit 인스턴스를 제공합니다.
     *
     * @param okHttpClient OkHttpClient 인스턴스
     * @return GeocodingApi와 연결된 Retrofit 인스턴스
     */
    @GeocodingRetrofit // GeocodingRetrofit 애노테이션을 사용하여 특정 Retrofit 인스턴스를 구별
    @Provides
    @Singleton
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/") // GeocodingApi의 BaseUrl
            .client(okHttpClient) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create()) // Gson을 이용한 응답 변환기 설정
            .build() // Retrofit 인스턴스 생성
    }

    /**
     * ClothingBinApi를 제공합니다.
     *
     * @param retrofit ClothingBinRetrofit을 사용하여 생성된 Retrofit 인스턴스
     * @return ClothingBinApi 인스턴스
     */
    @Provides
    @Singleton
    fun provideClothingBinApi(@ClothingBinRetrofit retrofit: Retrofit): ClothingBinApi {
        return retrofit.create(ClothingBinApi::class.java) // Retrofit으로 API 인터페이스 구현체 생성
    }

    /**
     * GenericClothingBinApiHandler를 제공합니다.
     *
     * @param clothingBinApi ClothingBinApi 인스턴스.
     * @return GenericClothingBinApiHandler 인스턴스.
     *
     * - GenericClothingBinApiHandler는 ClothingBinApi를 활용하여 구체적인 API 호출을 관리하는 클래스입니다.
     */
    @Provides
    @Singleton
    fun provideGenericClothingBinApiHandler(clothingBinApi: ClothingBinApi): ClothingBinApiHandler {
        return ClothingBinApiHandler(clothingBinApi)
    }

    /**
     * GeocodingApi를 제공합니다.
     *
     * @param retrofit GeocodingRetrofit을 사용하여 생성된 Retrofit 인스턴스
     * @return GeocodingApi 인스턴스
     */
    @Provides
    @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi {
        return retrofit.create(GeocodingApi::class.java) // Retrofit으로 API 인터페이스 구현체 생성
    }
}

/**
 * ClothingBin API에 대한 Retrofit 인스턴스를 구별하기 위한 커스텀 어노테이션
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClothingBinRetrofit

/**
 * Geocoding API에 대한 Retrofit 인스턴스를 구별하기 위한 커스텀 어노테이션
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit