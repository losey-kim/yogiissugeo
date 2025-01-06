package com.yogiissugeo.android.ui.setting

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.yogiissugeo.android.data.model.RecyclingInfo
import com.yogiissugeo.android.data.model.RecyclingInfoResponse
import com.yogiissugeo.android.utils.config.RemoteConfigKeys
import com.yogiissugeo.android.utils.config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * 설정 화면 데이터를 관리하는 ViewModel 클래스.
 **/
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {
    //재활용 정보 목록을 저장
    private val _recyclingInfo = MutableStateFlow<List<RecyclingInfo>>(emptyList())
    val recyclingInfo: StateFlow<List<RecyclingInfo>> = _recyclingInfo

    init {
        parseRecyclingInfo()
    }

    //Remote Config에서 재활용 정보를 가져와서 [RecyclingInfo] 객체 목록으로 파싱
    private fun parseRecyclingInfo() {
        val json = remoteConfigManager.getRemoteConfigValue(RemoteConfigKeys.RECYCLING_INFO)
        try {
            val response = Gson().fromJson(json, RecyclingInfoResponse::class.java)
            val recyclingInfoMap = response.recyclingInfo
            _recyclingInfo.value = recyclingInfoMap
        } catch (e: Exception) {
            _recyclingInfo.value = emptyList() // 파싱 실패 시 기본값 설정
        }
    }
}