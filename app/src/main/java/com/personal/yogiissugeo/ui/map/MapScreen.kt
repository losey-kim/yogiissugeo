package com.personal.yogiissugeo.ui.map

import android.graphics.Color
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.Clusterer.ComplexBuilder
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultClusterOnClickListener
import com.naver.maps.map.clustering.DefaultDistanceStrategy
import com.naver.maps.map.clustering.DefaultMarkerManager
import com.naver.maps.map.clustering.DistanceStrategy
import com.naver.maps.map.clustering.Node
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.personal.yogiissugeo.R
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBin
import com.personal.yogiissugeo.ui.list.BinListViewModel

@Composable
fun NaverMapScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    mapViewModel : MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴

    //지도 관련
    val mapView = mapViewModel.mapView
    val naverMapState by mapViewModel.naverMapState.collectAsState() // ViewModel의 상태 관찰

    //의류수거함 값
    val clothingBins = binListViewModel.clothingBins.collectAsState().value
    val selectedApiSource = binListViewModel.selectedApiSource.collectAsState().value

    //위치권한 관련
    val activity = context as ComponentActivity // 현재 Activity 가져오기
    val locationSource = remember {
        FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // MapView의 Lifecycle 이벤트를 관리
    ManageMapViewLifecycle(lifecycle, mapView, naverMapState)

    Box(modifier = Modifier.fillMaxSize()) {
        // AndroidView로 MapView를 Compose UI에 포함
        AndroidView(
            factory = { mapView }, // MapView를 생성
            modifier = Modifier
                .fillMaxSize()
        ) { mapView ->
            // MapView가 준비되었을 때 호출되는 콜백
            mapView.getMapAsync { naverMap ->
                if (naverMapState == null){
                    mapViewModel.setNaverMapState(naverMap) // NaverMap 상태 업데이트
                    setupNaverMap(naverMap, selectedApiSource) // NaverMap 설정
                }
                if (clothingBins.isNotEmpty()){
                    setupMarker(naverMap, clothingBins)
                }
            }
        }

        Button(
            onClick = {
                // "더보기" 버튼 클릭 시 현재 좌표 전달
                binListViewModel.goToNextPage(100)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("더보기")
        }
    }

    // NaverMap이 준비되었을 때 권한 요청 처리
    naverMapState?.let { naverMap ->
        HandlePermissions(naverMap, locationSource) // NaverMap을 전달
    }
}

// 지도 초기화 및 설정 함수
private fun setupNaverMap(
    naverMap: NaverMap,
    selectedApiSource: ApiSource?
) {
    naverMap.cameraPosition = CameraPosition(
        selectedApiSource?.defaultLatLng ?: NaverMap.DEFAULT_CAMERA_POSITION.target,
        12.0,
        0.0,
        0.0
    )
    naverMap.uiSettings.isCompassEnabled = true // 나침반 버튼 활성화
    naverMap.uiSettings.isLocationButtonEnabled = true //현위치 버튼 활성화
}

//마커 생성함수
private fun setupMarker(
    naverMap: NaverMap,
    clothingBins: List<ClothingBin>,
){
    // 의류 수거함 데이터를 기반으로 키와 태그 맵 생성
    val keyTagMap = buildMap {
        clothingBins.fastForEachIndexed { i, bin ->
            put(ItemKey(i, LatLng((bin.latitude?.toDouble() ?: 0.0), (bin.longitude?.toDouble() ?: 0.0))), ItemData(bin.address ?: "", bin.district ?: ""))
        }
    }

    var clusterer: Clusterer<ItemKey>? = null

    // 클러스터링 설정 및 생성
    clusterer = ComplexBuilder<ItemKey>()
        .minClusteringZoom(9) // 최소 클러스터링 줌 레벨
        .maxClusteringZoom(16) // 최대 클러스터링 줌 레벨
        .maxScreenDistance(200.0) // 클러스터링 허용 최대 화면 거리
        .thresholdStrategy { zoom -> // 줌 레벨에 따른 클러스터링 기준 거리 설정
            if (zoom <= 11) {
                0.0
            } else {
                70.0
            }
        }
        .distanceStrategy(object : DistanceStrategy {
            private val defaultDistanceStrategy = DefaultDistanceStrategy()

            override fun getDistance(zoom: Int, node1: Node, node2: Node): Double {
                return if (zoom <= 9) {
                    // 줌 레벨 9 이하에서는 클러스터링 비활성화
                    -1.0
                } else if ((node1.tag as ItemData).gu == (node2.tag as ItemData).gu) {
                    // 같은 행정 구역 내에서만 클러스터링 적용
                    if (zoom <= 11) {
                        -1.0
                    } else {
                        defaultDistanceStrategy.getDistance(zoom, node1, node2)
                    }
                } else {
                    // 다른 행정 구역 간의 클러스터링 거리 설정
                    10000.0
                }
            }
        })
        .tagMergeStrategy { cluster -> // 클러스터 태그 병합 전략 설정
            if (cluster.maxZoom <= 9) {
                null
            } else {
                ItemData("", (cluster.children.first().tag as ItemData).gu)
            }
        }
        .markerManager(object : DefaultMarkerManager() { // 마커 생성 및 스타일 관리
            override fun createMarker() = super.createMarker().apply {
                subCaptionTextSize = 10f
                subCaptionColor = Color.WHITE
                subCaptionHaloColor = Color.TRANSPARENT
            }
        })
        .clusterMarkerUpdater { info, marker -> // 클러스터 마커 업데이트 로직
            val size = info.size
            marker.icon = when {
                info.minZoom <= 10 -> MarkerIcons.CLUSTER_HIGH_DENSITY
                size < 10 -> MarkerIcons.CLUSTER_LOW_DENSITY
                else -> MarkerIcons.CLUSTER_MEDIUM_DENSITY
            }
            marker.subCaptionText = if (info.minZoom == 10) {
                (info.tag as ItemData).gu // 행정 구역명 표시
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

    // 생성된 클러스터에 데이터 추가 및 지도와 연결
    clusterer.addAll(keyTagMap)
    clusterer.map = naverMap
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
fun HandlePermissions(
    naverMap: NaverMap,
    locationSource: FusedLocationSource
) {
    val context = LocalContext.current // 현재 Context를 가져옴

    // 권한 요청을 처리하는 ActivityResultLauncher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), // 여러 권한 요청을 위한 계약
        onResult = { permissions -> // 권한 요청 결과 콜백
            val isGranted = permissions.values.all { it } // 모든 권한이 승인되었는지 확인
            if (isGranted) { //권한이 허용된 경우
                // NaverMap 설정 및 위치 추적 모드 설정
                setupNaverMapWithLocationTracking(naverMap, locationSource)
            } else { // 권한이 거부된 경우
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

//클러스터링에서 사용되는 키 클래스
private class ItemKey(val id: Int, private val latLng: LatLng) : ClusteringKey {
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

//클러스터링 마커의 추가 정보를 저장하는 데이터 클래스
private class ItemData(val name: String, val gu: String)

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000