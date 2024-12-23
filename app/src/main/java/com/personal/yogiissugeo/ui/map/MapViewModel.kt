package com.personal.yogiissugeo.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

//NaverMap과 관련된 상태를 관리하는 ViewModel
@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    //MapView 인스턴스
    private val _mapView: MapView by lazy { MapView(appContext) }
    val mapView: MapView
        get() = _mapView

    //NaverMap 상태
    private val _naverMapState by lazy { MutableStateFlow<NaverMap?>(null) }
    val naverMapState: StateFlow<NaverMap?> = _naverMapState

    /**
     * NaverMap 상태를 설정하는 함수.
     * 최초 상태가 null인 경우에만 NaverMap 객체를 설정합니다.
     *
     * @param naverMap 초기화할 NaverMap 객체.
     */
    fun setNaverMapState(naverMap: NaverMap) {
        if (_naverMapState.value == null) {
            _naverMapState.value = naverMap
        }
    }

    /**
     * ViewModel이 클리어될 때 호출되는 함수.
     * - MapView의 생명주기와 관련된 리소스를 정리하기 위해 onDestroy를 호출
     */
    override fun onCleared() {
        super.onCleared()
        _mapView.onDestroy()
    }
}