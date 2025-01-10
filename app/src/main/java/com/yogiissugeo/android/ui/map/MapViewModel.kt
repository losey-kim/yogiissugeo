package com.yogiissugeo.android.ui.map

import androidx.lifecycle.ViewModel
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.clustering.Clusterer
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.utils.cluster.ItemKey
import com.yogiissugeo.android.utils.cluster.createItemKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

//NaverMap과 관련된 상태를 관리하는 ViewModel
@HiltViewModel
class MapViewModel @Inject constructor(
) : ViewModel() {
    //Cluster
    private val _clustererDistrict = MutableStateFlow<Clusterer<ItemKey>?>(null)
    val clustererDistrict: StateFlow<Clusterer<ItemKey>?> = _clustererDistrict

    //북마크 Cluster
    private val _clustererBookmarked = MutableStateFlow<Clusterer<ItemKey>?>(null)
    val clustererBookmarked: StateFlow<Clusterer<ItemKey>?> = _clustererBookmarked

    // Key->ClothingBin
    private val _keyTagMapAll = MutableStateFlow<MutableMap<ItemKey, ClothingBin>>(mutableMapOf())
    private val _keyTagMapBookmarked = MutableStateFlow<MutableMap<ItemKey, ClothingBin>>(mutableMapOf())

    /**
     * 구별 클러스터 세팅
     */
    fun setClustererDistrict(newClusterer: Clusterer<ItemKey>) {
        _clustererDistrict.value = newClusterer
    }

    /**
     * 북마크 클러스터 세팅
     */
    fun setClustererBookmark(newClusterer: Clusterer<ItemKey>) {
        _clustererBookmarked.value = newClusterer
    }

    /**
     * 의류수거함 전체 목록을 받아서, 클러스터를 갱신 (clear -> addAll) 하는 예시
     */
    fun updateClusterAll(items: List<ClothingBin>) {
        // 1) 기존 clear
        _clustererDistrict.value?.clear()
        _keyTagMapAll.value.clear()
        // 2) 새 데이터 채우기
        items.forEach { bin ->
            val key = createItemKey(bin) ?: return@forEach
            _keyTagMapAll.value[key] = bin
        }
        // 3) 한 번에 addAll
        _clustererDistrict.value?.addAll(_keyTagMapAll.value)
    }

    /**
     * 아이템 여러 개 추가
     */
    fun addDistrictItems(items: List<ClothingBin>) {
        val newMap = mutableMapOf<ItemKey, ClothingBin>()
        items.forEach { bin ->
            val key = createItemKey(bin) ?: return@forEach
            // 북마크 상태인 아이템이라면 District 클러스터엔 넣지 않는다
            if (!bin.isBookmarked) {
                _keyTagMapAll.value[key] = bin
                newMap[key] = bin
            }
        }
        // 한 번에 추가
        _clustererDistrict.value?.addAll(newMap)
    }

    /**
     * 아이템 하나 추가
     */
    fun addItem(item: ClothingBin) {
        val key = createItemKey(item) ?: return
        _keyTagMapAll.value[key] = item
        _clustererDistrict.value?.add(key, item)
    }

    /**
     * 특정 아이템 제거
     */
    fun removeItem(binId: String) {
        val entry = _keyTagMapAll.value.entries.find { it.value.id == binId } ?: return
        _keyTagMapAll.value.remove(entry.key)
        _clustererDistrict.value?.remove(entry.key)
    }

    /**
     * 전체 제거
     */
    fun clearAll() {
        _clustererDistrict.value?.clear()
        _keyTagMapAll.value.clear()
    }

    /**
     * 북마크 아이템 업데이트
     */
    //TODO 함수 개선 필요
    fun updateBookmarkedItems(items: List<ClothingBin>, currentDistrict: ApiSource?) {
        val clustererBm = _clustererBookmarked.value ?: return
        val clustererDistrict = _clustererDistrict.value

        // 1) 새로 들어온 bookmark 목록 → newSet
        val newMap = mutableMapOf<ItemKey, ClothingBin>()
        items.forEach { bin ->
            val key = createItemKey(bin) ?: return@forEach
            newMap[key] = bin
        }
        val newSet = newMap.keys

        // 2) 현재 클러스터(기존) 내부 map → oldSet
        // keyTagMapBookmarked는 "현재 bookmark된 아이템"을 담고 있음
        val oldMap = _keyTagMapBookmarked.value
        val oldSet = oldMap.keys

        // 3) 추가되어야 하는 키
        val addedKeys = newSet - oldSet

        // 4) 제거되어야 하는 키
        val removedKeys = oldSet - newSet

        // 5) "제거" 대상: removedKeys
        if (removedKeys.isNotEmpty()) {
            // 북마크 클러스터에서 제거할 맵 만들기
            val removingMap = removedKeys.associateWith { oldKey ->
                // oldMap에는 key -> bin이 있으므로, 값 찾을 수 있음
                oldMap[oldKey]
            }.filterValues { it != null } // null 제외
                .mapValues { (_, v) -> v!! }

            // oldMap에서도 제거
            removedKeys.forEach { oldMap.remove(it) }

            // 북마크 클러스터에서 배치 제거
            clustererBm.removeAll(removingMap.keys)

            // 현재 선택된 구와 같다면 구별 클러스터에 추가
            clustererDistrict?.addAll(removingMap.filterValues { bin ->
                bin.district == currentDistrict?.name
            })
        }

        // 6) "추가" 대상: addedKeys
        if (addedKeys.isNotEmpty()) {
            // 북마크 클러스터에 추가할 맵 만들기
            val addingMap = addedKeys.associateWith { newKey ->
                newMap[newKey]
            }.filterValues { it != null }
                .mapValues { (_, v) -> v!! }

            // oldMap에도 반영
            oldMap.putAll(addingMap)

            // District에서 "추가 키"를 제거, (북마크가 되었다면, district에서 안 보이게)
            clustererDistrict?.removeAll(addedKeys)

            // 북마크 클러스터에 일괄 추가
            clustererBm.addAll(addingMap)
        }
    }

    // CameraPosition
    private val _cameraPosition = MutableStateFlow<CameraPosition?>(null)
    val cameraPosition: StateFlow<CameraPosition?> = _cameraPosition

    fun setCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }
}