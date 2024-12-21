package com.personal.yogiissugeo.utils.config

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.naver.maps.map.NaverMapSdk
import com.personal.yogiissugeo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    @ApplicationContext private val context: Context
) {

    // RemoteConfig 초기화 완료 상태를 추적하기 위한 StateFlow
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    /**
     * RemoteConfig 초기화 메서드
     * - 기본값을 설정하고, fetchAndActivate를 호출하여 RemoteConfig 값을 가져옵니다.
     * - 초기화 완료 후, Naver 지도 SDK를 설정.
     */
    fun initialize() {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val naverClientId = remoteConfig.getString(RemoteConfigKeys.NAVER_MAP_CLIENT_ID)
                    initializeNaverMapSdk(naverClientId)
                }
                _isInitialized.value = true
            }
    }

    /**
     * Naver 지도 SDK를 초기화하는 메서드
     * @param clientId Naver Cloud Platform에서 발급받은 클라이언트 ID
     */
    private fun initializeNaverMapSdk(clientId: String) {
        NaverMapSdk.getInstance(context).client = NaverMapSdk.NaverCloudPlatformClient(clientId)
    }

    /**
     * RemoteConfig에서 특정 키의 값을 가져오는 메서드  (단순 반환)
     * @param key RemoteConfig 키
     * @return 키에 해당하는 값
     */
    fun getRemoteConfigValue(key: String): String {
        return remoteConfig.getString(key)
    }

    /**
     * Firebase RemoteConfig에서 특정 키 값을 가져오는 함수.
     * 값이 존재하지 않는 경우, fetch 작업을 수행하여 최신 값을 가져옵니다.
     *
     * @param key RemoteConfig 키
     * @return 키에 해당하는 값, 없으면 null을 반환.
     */
    suspend fun getRemoteConfigValueWithFetch(key: String): String? {
        val value = remoteConfig.getString(key)
        if (value.isNotEmpty()) {
            // 이미 유효한 값이 있으면 반환
            return value
        } else {
            // 값이 없으면 fetch 재시도
            remoteConfig.fetchAndActivate().await()
            val newValue = remoteConfig.getString(key)
            // 새로 가져온 값 반환, fetch 실패 시 null 반환
            return newValue.takeIf { it.isNotEmpty() }
        }
    }
}