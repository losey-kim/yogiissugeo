package com.yogiissugeo.android.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogiissugeo.android.utils.common.VersionUtils
import com.yogiissugeo.android.utils.config.RemoteConfigKeys
import com.yogiissugeo.android.utils.config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 스플래시 데이터를 관리하는 뷰모델
 **/
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // RemoteConfig 초기화여부 저장
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // 강제업데이트 여부
    private val _forceUpdate = MutableStateFlow(false)
    val forceUpdate: StateFlow<Boolean> = _forceUpdate

    init {
        initializeRemoteConfig()
    }

    /**
     * Firebase RemoteConfig 초기화
     */
    private fun initializeRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.initialize {
                // 초기화 여부 저장
                _isInitialized.value = true
                // 강제업데이트 여부 확인
                isForceUpdate()
            }
        }
    }

    /**
     * 강제업데이트 필요 여부
     */
    private fun isForceUpdate() {
        viewModelScope.launch {
            // 현재 버전
            val currentVersion = VersionUtils.getCurrentAppVersion(context)
            // 강제 업데이트 버전
            val forceUpdateVersion =
                remoteConfigManager.getRemoteConfigValueNumber(RemoteConfigKeys.FORCE_UPDATE_VERSION)
            // 현재 버전이 낮다면 강제업데이트 true
            _forceUpdate.value = currentVersion < forceUpdateVersion
        }
    }
}