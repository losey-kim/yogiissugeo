package com.personal.yogiissugeo

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.personal.yogiissugeo.utils.config.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ClothingBinApplication : Application() {

    // RemoteConfigManager를 Hilt를 통해 주입
    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate() {
        super.onCreate()
        // 전역 초기화 작업 (필요할 경우)

        // 앱 실행 시 RemoteConfig 초기화
        remoteConfigManager.initialize()

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // 백그라운드 스레드에서  Google Mobile Ads SDK 초기화
            MobileAds.initialize(this@ClothingBinApplication) {

            }
        }
    }
}