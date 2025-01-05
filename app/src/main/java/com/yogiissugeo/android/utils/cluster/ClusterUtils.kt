package com.yogiissugeo.android.utils.cluster

import android.graphics.Color
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultClusterOnClickListener
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ClothingBin

// 구별 마커 업데이트 로직
fun updateDistrictClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
    val size = info.size
    marker.icon = when {
        info.minZoom <= 10 -> OverlayImage.fromResource(R.drawable.ic_cluster_high_distiny)
        size < 10 -> OverlayImage.fromResource(R.drawable.ic_cluster_low_distiny)
        else -> OverlayImage.fromResource(R.drawable.ic_cluster_medium_distiny)
    }
    marker.subCaptionText = if (info.minZoom == 10) {
        (info.tag as? ClothingBin)?.address.orEmpty()
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

// 구별 마커 업데이트 로직
fun updateDistrictLeafMarker(
    info: LeafMarkerInfo,
    marker: Marker,
    onMarkerClick: (String) -> Unit
) {
    val itemData = info.tag as? ClothingBin
    marker.icon = OverlayImage.fromResource(R.drawable.ic_marker_add)
    marker.anchor = Marker.DEFAULT_ANCHOR
    marker.captionText = itemData?.address.orEmpty()
    marker.setCaptionAligns(Align.Bottom)
    marker.captionColor = Color.BLACK
    marker.captionHaloColor = Color.WHITE
    marker.subCaptionText = ""
    marker.onClickListener = Overlay.OnClickListener {
        itemData?.id?.let(onMarkerClick)
        true
    }
}

// 북마크 마커 업데이트 로직
fun updateBookmarkClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
    val size = info.size
    marker.icon = OverlayImage.fromResource(R.drawable.ic_cluster_bookmark)
    marker.subCaptionText = if (info.minZoom == 10) {
        (info.tag as? ClothingBin)?.district.orEmpty()
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

// 북마크 마커 업데이트 로직
fun updateBookmarkLeafMarker(
    info: LeafMarkerInfo,
    marker: Marker,
    onMarkerClick: (String) -> Unit
) {
    val itemData = info.tag as? ClothingBin
    marker.icon = OverlayImage.fromResource(R.drawable.ic_marker_bookmark)
    marker.anchor = Marker.DEFAULT_ANCHOR
    marker.captionText = itemData?.address.orEmpty()
    marker.setCaptionAligns(Align.Bottom)
    marker.captionColor = Color.BLACK
    marker.captionHaloColor = Color.WHITE
    marker.subCaptionText = ""
    marker.onClickListener = Overlay.OnClickListener {
        itemData?.id?.let(onMarkerClick)
        true
    }
}

// ItemKey 생성 헬퍼
fun createItemKey(bin: ClothingBin): ItemKey? {
    return try {
        val latitude = bin.latitude?.toDoubleOrNull() ?: 0.0
        val longitude = bin.longitude?.toDoubleOrNull() ?: 0.0
        ItemKey(bin.id.hashCode(), LatLng(latitude, longitude))
    } catch (e: Exception) {
        null // 예외 발생 시 null 반환
    }
}

//클러스터링에서 사용되는 키 클래스
class ItemKey(val id: Int, private val latLng: LatLng) : ClusteringKey {
    //마커의 위치를 반환
    override fun getPosition() = latLng

    //두 ID 값을 비교하여 ItemKey 객체가 동일한지 비교
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val itemKey = other as ItemKey
        return id == itemKey.id
    }

    //ID 값을 기반으로 해시 코드를 생성
    override fun hashCode() = id
}