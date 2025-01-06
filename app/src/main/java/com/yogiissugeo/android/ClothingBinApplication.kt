package com.yogiissugeo.android

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.naver.maps.map.NaverMapSdk
import com.yogiissugeo.android.utils.config.RemoteConfigKeys
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClothingBinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 전역 초기화 작업 (필요할 경우)

        //광고 초기화
        MobileAds.initialize(this@ClothingBinApplication)

        //네이버 지도 SDK 초기화
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_MAP_CLIENT_ID)
    }
}