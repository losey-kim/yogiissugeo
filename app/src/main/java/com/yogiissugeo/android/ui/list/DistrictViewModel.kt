package com.yogiissugeo.android.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.SupportedDistricts
import com.yogiissugeo.android.utils.config.RemoteConfigKeys
import com.yogiissugeo.android.utils.config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DistrictViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {

    // 지원되는 District 데이터를 저장하는 StateFlow
    private val _districts = MutableStateFlow<List<ApiSource>>(emptyList())
    val districts: StateFlow<List<ApiSource>> = _districts

    init {
        // RemoteConfig 초기화 및 변경 상태를 관찰
        observeRemoteConfigStates()
    }

    /**
     * RemoteConfig 초기화 및 업데이트 상태를 하나의 Flow로 관찰하고, 조건에 따라 데이터를 로드합니다.
     */
    private fun observeRemoteConfigStates() {
        viewModelScope.launch {
            // isInitialized와 isUpdated 두 Flow를 결합하여 상태를 Pair로
            combine(
                remoteConfigManager.isInitialized, // RemoteConfig 초기화 상태를 나타내는 Flow
                remoteConfigManager.isUpdated // RemoteConfig 업데이트 상태를 나타내는 Flow
            ) { isInitialized, isUpdated ->
                // 두 상태를 Pair로 반환
                Pair(isInitialized, isUpdated)
                // 동일한 상태 변화에 대해 중복 처리를 방지 (중복된 상태 변경을 무시)
            }.distinctUntilChanged()
                // 결합된 Flow를 collect하여 상태 변화에 따라 로직을 실행
                .collect { (isInitialized, isUpdated) ->
                    // 초기화되었거나 업데이트되었을 때
                    if (isInitialized || isUpdated) {
                        // RemoteConfig에서 지원되는 District 데이터를 로드
                        loadSupportedDistricts()

                        // 업데이트가 완료된 경우, 업데이트 플래그를 초기화
                        if (isUpdated) {
                            remoteConfigManager.resetUpdateValue()
                        }
                    }
                }
        }
    }

    /**
     * RemoteConfig에서 지원되는 District 데이터를 가져와 상태를 업데이트합니다.
     */
    private fun loadSupportedDistricts() {
        val json = remoteConfigManager.getRemoteConfigValue(RemoteConfigKeys.SUPPORTED_DISTRICTS)
        val supportedDistricts = parseSupportedDistricts(json)
        _districts.value = supportedDistricts
    }

    /**
     * JSON 문자열을 파싱하여 지원되는 District 목록을 반환합니다.
     * @param json RemoteConfig에서 가져온 JSON 문자열
     * @return 지원되는 District 목록
     */
    private fun parseSupportedDistricts(json: String): List<ApiSource> {
        return try {
            val gson = Gson()
            val supportedKeys = gson.fromJson(json, SupportedDistricts::class.java)
            ApiSource.entries.filter { it.name in supportedKeys.supportedDistricts }
        } catch (e: Exception) {
            emptyList()
        }
    }
}