package com.yogiissugeo.android.utils.config

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.yogiissugeo.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {

    // RemoteConfig 초기화 완료 상태를 추적하기 위한 StateFlow
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // RemoteConfig 업데이트 상태를 추적하기 위한 StateFlow
    private val _isUpdated = MutableStateFlow(false)
    val isUpdated: StateFlow<Boolean> = _isUpdated

    /**
     * RemoteConfig 초기화 메서드
     * - 기본값을 설정하고, fetchAndActivate를 호출하여 RemoteConfig 값을 가져옵니다.
     * - 초기화 완료 후, Naver 지도 SDK를 설정.
     */
    fun initialize(onComplete: () -> Unit) {
        //기본값 설정
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        //Config값 가져오기
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                _isInitialized.value = true
                onComplete.invoke()
            }

        //Config값 Updated 시 호출
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                remoteConfig.activate().addOnCompleteListener {
                    //업데이트 성공
                    _isUpdated.value = true
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
            }
        })
    }

    /**
     * 업데이트 알림 후 변수 초기화
     */
    fun resetUpdateValue() {
        _isUpdated.value = false
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
     * RemoteConfig에서 특정 키의 값을 가져오는 메서드 (숫자타입)
     * @param key RemoteConfig 키
     * @return 키에 해당하는 값
     */
    fun getRemoteConfigValueNumber(key: String): Long {
        return remoteConfig.getLong(key)
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