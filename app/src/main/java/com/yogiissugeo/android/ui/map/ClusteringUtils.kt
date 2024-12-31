package com.yogiissugeo.android.ui.map

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusteringKey

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