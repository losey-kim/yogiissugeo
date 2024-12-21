package com.personal.yogiissugeo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.SupportedDistricts
import com.personal.yogiissugeo.utils.config.RemoteConfigKeys
import com.personal.yogiissugeo.utils.config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        // RemoteConfig 초기화 상태를 관찰
        observeRemoteConfigState()
    }

    /**
     * RemoteConfig 초기화 상태를 관찰하고, 초기화가 완료되면 데이터를 로드합니다.
     */
    private fun observeRemoteConfigState() {
        viewModelScope.launch {
            remoteConfigManager.isInitialized.collect { isInitialized ->
                if (isInitialized) {
                    loadSupportedDistricts()
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
        } catch (e:Exception){
            emptyList()
        }
    }
}