package com.personal.yogiissugeo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

//맵뷰의 생명주기 관리
@Composable
fun ManageMapViewLifecycle(
    lifecycle: Lifecycle,
    mapView: MapView,
    naverMapState: NaverMap?
) {
    DisposableEffect(lifecycle) {
        // MapView의 생명주기를 Android Lifecycle과 동기화하는 Observer 정의
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                // MapView가 아직 초기화되지 않은 경우에만 onCreate 호출 (재초기화 방지)
                if (naverMapState == null) mapView.onCreate(null) // MapView 생성
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart() //MapView 시작
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume() //MapView 활성화
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause() //MapView 일시 중지
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop() //MapView 정지
            }
        }

        // Lifecycle에 Observer를 추가
        lifecycle.addObserver(lifecycleObserver)

        // DisposableEffect가 해제될 때 Observer 제거
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}