package com.yogiissugeo.android.ui.map

import android.graphics.Color
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultClusterOnClickListener
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.yogiissugeo.android.data.model.ClothingBin

private const val DEFAULT_ZOOM_LEVEL = 12.0

// 지도 초기화 및 설정 함수
fun setupNaverMap(
    naverMap: NaverMap
) {
    naverMap.cameraPosition = CameraPosition(
        NaverMap.DEFAULT_CAMERA_POSITION.target,
        DEFAULT_ZOOM_LEVEL,
        0.0,
        0.0
    )

    naverMap.uiSettings.apply {
        isCompassEnabled = true // 나침반 버튼 활성화
        isLocationButtonEnabled = true // 현위치 버튼 활성화
    }
}

//카메라 이동 함수
fun animateCameraToPosition(latitude: Double, longitude: Double, naverMap: NaverMap) {
    naverMap.moveCamera(
        CameraUpdate.toCameraPosition(
            CameraPosition(
                LatLng(latitude, longitude),
                DEFAULT_ZOOM_LEVEL
            )
        ).animate(
            CameraAnimation.Easing, NaverMap.DEFAULT_DEFAULT_CAMERA_ANIMATION_DURATION.toLong()
        )
    )
}

//클러스터 생성
private fun createClusterer(
): Clusterer<ItemKey> {
    return Clusterer.Builder<ItemKey>()
        .clusterMarkerUpdater { info, marker -> // 클러스터 마커 업데이트 로직
            val size = info.size
            marker.icon = when {
                info.minZoom <= 10 -> MarkerIcons.CLUSTER_HIGH_DENSITY
                size < 10 -> MarkerIcons.CLUSTER_LOW_DENSITY
                else -> MarkerIcons.CLUSTER_MEDIUM_DENSITY
            }
            marker.subCaptionText = if (info.minZoom == 10) {
                ((info.tag as? ItemData)?.district) ?: ""// 행정 구역명 표시
            } else {
                ""
            }
            marker.anchor = DefaultClusterMarkerUpdater.DEFAULT_CLUSTER_ANCHOR
            marker.captionText = size.toString()
            marker.setCaptionAligns(Align.Center)
            marker.captionColor = Color.WHITE
            marker.captionHaloColor = Color.TRANSPARENT
            marker.onClickListener = DefaultClusterOnClickListener(info)
        }
        .leafMarkerUpdater { info, marker -> // 개별 마커 업데이트 로직
            marker.icon = Marker.DEFAULT_ICON
            marker.anchor = Marker.DEFAULT_ANCHOR
            marker.captionText = (info.tag as ItemData).name
            marker.setCaptionAligns(Align.Bottom)
            marker.captionColor = Color.BLACK
            marker.captionHaloColor = Color.WHITE
            marker.subCaptionText = ""
            marker.onClickListener = null
        }
        .build()
}

//클러스터 추가
fun addCluster(
    currentClusterer: Clusterer<ItemKey>?,
    currentTagMap: Map<ItemKey, ItemData>?,
    naverMap: NaverMap,
    clothingBins: List<ClothingBin>,
    onAddClusterer: (Clusterer<ItemKey>, Map<ItemKey, ItemData>) -> Unit
) {
    //기존 클러스터 초기화
    currentClusterer?.clear()

    // 의류 수거함 데이터를 기반으로 키와 태그 맵 생성
    val newKeyTagMap = buildMap {
        clothingBins.fastForEach { bin ->
            val keyData = try {
                val lat = bin.latitude?.toDouble() ?: 0.0
                val lng = bin.longitude?.toDouble() ?: 0.0
                val key = ItemKey(bin.id.hashCode(), LatLng(lat, lng))
                val data = ItemData(bin.address.orEmpty(), bin.district.orEmpty())
                key to data
            } catch (e: NumberFormatException) {
                null // 예외 발생 시 null 반환
            }
            keyData?.let { (key, data) -> put(key, data) }
        }
    } + (currentTagMap ?: emptyMap())

    // 클러스터 생성
    val newClusterer = createClusterer()

    // 생성된 클러스터에 데이터 추가
    newClusterer.addAll(newKeyTagMap)
    newClusterer.map = naverMap
    onAddClusterer.invoke(newClusterer, newKeyTagMap)
}

// NaverMap 설정 및 위치 추적 모드 설정
fun setupNaverMapWithLocationTracking(
    naverMap: NaverMap,
    locationSource: FusedLocationSource
) {
    // 지도 초기 설정 (옵션)
    naverMap.uiSettings.isLocationButtonEnabled = true // 위치 버튼 활성화
    naverMap.locationSource = locationSource // 위치 소스 설정

    //TODO 설정페이지에 네이버 지도 라이선스 연결해야함
    naverMap.uiSettings.isLogoClickEnabled = false
}