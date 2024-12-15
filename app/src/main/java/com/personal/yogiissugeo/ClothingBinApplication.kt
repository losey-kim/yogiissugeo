package com.personal.yogiissugeo

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClothingBinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 전역 초기화 작업 (필요할 경우)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.GEOCODING_API_KEY_ID)
    }
}