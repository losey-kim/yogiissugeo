package com.personal.yogiissugeo.ui.map

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.personal.yogiissugeo.R

@Composable
fun NaverMapScreen(
    latitude: Double,  // 지도에 표시할 중심 좌표의 위도
    longitude: Double // 지도에 표시할 중심 좌표의 경도
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val activity = context as ComponentActivity // 현재 Activity 가져오기
    val mapView = remember { MapView(context) } // MapView를 remember로 한 번만 생성하여 재사용
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴
    val bundle = remember { Bundle() } // 상태 복원을 위해 초기화된 Bundle

    val locationSource = remember {
        FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // MapView의 Lifecycle 이벤트를 관리
    DisposableEffect(mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(bundle) // MapView 생성
                Lifecycle.Event.ON_START -> mapView.onStart() // MapView 시작
                Lifecycle.Event.ON_RESUME -> mapView.onResume() // MapView 활성화
                Lifecycle.Event.ON_PAUSE -> mapView.onPause() // MapView 일시 중지
                Lifecycle.Event.ON_STOP -> mapView.onStop() // MapView 중지
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy() // MapView 제거
                else -> {}
            }
        }

        // LifecycleObserver를 Lifecycle에 추가
        lifecycle.addObserver(lifecycleObserver)

        // 컴포저블이 제거될 때 호출
        onDispose {
            lifecycle.removeObserver(lifecycleObserver) // Observer 제거
            mapView.onDestroy() // MapView 리소스 해제
        }
    }

    // AndroidView로 MapView를 Compose UI에 포함
    AndroidView(
        factory = { mapView }, // MapView를 생성
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding() // 화면 전체 크기로 MapView를 표시
    ) { mapView ->
        // MapView가 준비되었을 때 호출되는 콜백
        mapView.getMapAsync { naverMap ->
            setupNaverMap(naverMap, latitude, longitude, locationSource) // NaverMap 설정
        }
    }

    // 권한 요청 처리
    HandlePermissions()
}

// 지도 초기화 및 설정 함수
private fun setupNaverMap(
    naverMap: NaverMap,
    latitude: Double,
    longitude: Double,
    locationSource: FusedLocationSource
) {
    naverMap.uiSettings.isCompassEnabled = true // 나침반 버튼 활성화
    naverMap.uiSettings.isLocationButtonEnabled = true //현위치 버튼 활성화
    // 지도의 카메라를 특정 좌표로 이동
    val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
    naverMap.moveCamera(cameraUpdate)

    //마커 생성
    Marker().apply {
        position = LatLng(latitude, longitude)
        map = naverMap
    }

    // NaverMap 설정 및 위치 추적 모드 설정
    setupNaverMapWithLocationTracking(naverMap, locationSource)
}

// NaverMap 설정 및 위치 추적 모드 설정
private fun setupNaverMapWithLocationTracking(
    naverMap: NaverMap,
    locationSource: FusedLocationSource
) {
    // 지도 초기 설정 (옵션)
    naverMap.uiSettings.isLocationButtonEnabled = true // 위치 버튼 활성화
    naverMap.locationSource = locationSource // 위치 소스 설정
}

// 권한 요청 및 결과 처리
@Composable
fun HandlePermissions() {
    val context = LocalContext.current // 현재 Context를 가져옴

    // 권한 요청을 처리하는 ActivityResultLauncher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), // 여러 권한 요청을 위한 계약
        onResult = { permissions -> // 권한 요청 결과 콜백
            val isGranted = permissions.values.all { it } // 모든 권한이 승인되었는지 확인
            if (!isGranted) { // 권한이 거부된 경우
                Toast.makeText(
                    context,
                    R.string.permission_location, // 사용자에게 권한 필요 메시지 표시
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    // Compose의 `LaunchedEffect`로 권한 요청 트리거
    LaunchedEffect(Unit) {
        launcher.launch( // 권한 요청 시작
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION, // 정확한 위치 권한
                android.Manifest.permission.ACCESS_COARSE_LOCATION // 대략적인 위치 권한
            )
        )
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000