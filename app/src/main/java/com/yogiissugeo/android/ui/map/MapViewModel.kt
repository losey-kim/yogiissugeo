package com.yogiissugeo.android.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.clustering.Clusterer
import com.yogiissugeo.android.data.model.ClothingBin
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

    //Cluster
    private val _clusterer = MutableStateFlow<Clusterer<ItemKey>?>(null)
    val clusterer: StateFlow<Clusterer<ItemKey>?> = _clusterer

    //keyTagMap
    private val _keyTagMap = MutableStateFlow<Map<ItemKey?, ClothingBin>?>(null)
    val keyTagMap: StateFlow<Map<ItemKey?, ClothingBin>?> = _keyTagMap


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
     * 클러스터를 객체 설정
     *
     * @param newClusterer 초기화할 clusterer
     */
    fun setClusterer(newClusterer: Clusterer<ItemKey>) {
        _clusterer.value = newClusterer
    }

    /**
     * ItemKey와 ItemData의 매핑 데이터를 설정
     * null인 경우 초기화
     *
     * @param newKeyTagMap 새로운 매핑 데이터 (Map<ItemKey, ClothingBin>)
     */
    fun setKeyTagMap(newKeyTagMap: Map<ItemKey?, ClothingBin>?) {
        _keyTagMap.value = newKeyTagMap
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