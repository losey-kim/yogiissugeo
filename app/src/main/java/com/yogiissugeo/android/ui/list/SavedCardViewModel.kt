package com.yogiissugeo.android.ui.list

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import com.yogiissugeo.android.utils.common.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SavedCardViewModel @Inject constructor(
    private val shareUtils: ShareUtils
) : ViewModel() {

    /**
     * 네이버 지도 앱을 열기 위한 함수.
     * @param latLng 위치 정보 (위도와 경도)
     * @param address 마커에 표시될 주소
     */
    fun onNavigate(latLng: LatLng, address: String) {
        shareUtils.navigateToNaverMap(latLng, address)
    }

    /**
     * 주소를 공유하기 위한 함수.
     * @param address 공유할 주소
     */
    fun onShareAddress(address: String) {
        shareUtils.shareAddress(address)
    }
}