package com.personal.yogiissugeo

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.naver.maps.map.NaverMapSdk
import com.personal.yogiissugeo.data.constants.ApiKeys
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClothingBinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 전역 초기화 작업 (필요할 경우)

        // Firebase Remote Config 초기화 및 fetch
        val remoteConfig = Firebase.remoteConfig

        // RemoteConfig 값 불러오기
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { //가져오기 성공
                    //네이버 지도 SDK
                    val naverClientId = remoteConfig.getString(ApiKeys.NAVER_MAP_CLIENT_ID)
                    NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient(naverClientId)
                } else { //가져오기 실패
                }
            }

    }
}