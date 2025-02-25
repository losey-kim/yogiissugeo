package com.yogiissugeo.android.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.util.FusedLocationSource
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.Region
import com.yogiissugeo.android.ui.component.LoadingIndicator
import com.yogiissugeo.android.ui.component.DropDownButtonComponent
import com.yogiissugeo.android.ui.component.showSnackBar
import com.yogiissugeo.android.ui.list.BinListViewModel
import com.yogiissugeo.android.ui.list.DistrictViewModel
import com.yogiissugeo.android.ui.list.SharedMapViewModel
import com.yogiissugeo.android.utils.cluster.ItemKey
import com.yogiissugeo.android.utils.cluster.updateBookmarkClusterMarker
import com.yogiissugeo.android.utils.cluster.updateBookmarkLeafMarker
import com.yogiissugeo.android.utils.cluster.updateDistrictClusterMarker
import com.yogiissugeo.android.utils.cluster.updateDistrictLeafMarker

@Composable
fun NaverMapScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
    sharedViewModel: SharedMapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴

    // 의류수거함 관련 상태
    val clothingBins by binListViewModel.clothingBins.collectAsState()
    val bookmarkedBins by binListViewModel.allBookmarkedBins.collectAsState(emptyList())
    val supportDistricts by districtViewModel.districts.collectAsState() // 지원하는 구 목록 가져오기
    val isLoading by binListViewModel.isLoading.collectAsState()
    val errorMessage by binListViewModel.errorMessage.collectAsState()
    val selectedApiSource by binListViewModel.selectedApiSource.collectAsState()
    val currentPage by binListViewModel.currentPage.collectAsState()
    val totalPage by binListViewModel.totalPage.collectAsState()

    // MapView
    val mapView = remember { MapView(context) }
    val naverMapHolder = remember { mutableStateOf<NaverMap?>(null) }
    val cameraPos = mapViewModel.cameraPosition.collectAsState()

    // 로컬 상태
    val isMapInteracting = rememberSaveable { mutableStateOf(false) } // 지도 조작 상태
    val previousClothingBins = rememberSaveable { mutableStateOf(clothingBins) }  //의류수거함 값
    val previousBookmarkBins = rememberSaveable { mutableStateOf(clothingBins) }  //저장된 의류수거함 값
    val previousDistrict = rememberSaveable { mutableStateOf(selectedApiSource) }

    // 선택 좌표 상태
    val selectedCoordinates by sharedViewModel.selectedCoordinates.collectAsState()

    // SnackbarHostState 생성
    val snackbarHostState = remember { SnackbarHostState() }
    // 북마크 토글 결과 리소스 아이디
    var bookmarkResultResId by remember { mutableStateOf<Int?>(null) }

    // 상위 지역 선택 상태 (초기값은 null)
    var selectedRegion by rememberSaveable { mutableStateOf<Region?>(null) }
    var selectedDistrict by rememberSaveable { mutableStateOf<ApiSource?>(null) }

    // MapView의 Lifecycle 관리
    ManageMapViewLifecycle(lifecycle, mapView)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        // AndroidView로 MapView를 Compose UI에 포함
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMapHolder.value = naverMap
                        // 지도 기본 설정
                        setupNaverMap(naverMap, cameraPos.value)

                        // 카메라 이벤트 설정
                        naverMap.addOnCameraChangeListener { reason, _ ->
                            //사용자의 버튼 선택으로 인해 카메라가 움직였음
                            if (reason == CameraUpdate.REASON_GESTURE) {
                                isMapInteracting.value = true
                            }
                        }

                        // 카메라 대기 이벤트
                        naverMap.addOnCameraIdleListener {
                            isMapInteracting.value = false
                            // 카메라 멈출 때 ViewModel에 위치 저장
                            mapViewModel.setCameraPosition(naverMap.cameraPosition)
                        }

                        //구별 클러스터 초기화
                        if (mapViewModel.clustererDistrict.value == null) {
                            val newClustererDistrict = Clusterer.Builder<ItemKey>()
                                // 클러스터 마커(집합 마커) 업데이트 로직
                                .clusterMarkerUpdater { info, marker ->
                                    updateDistrictClusterMarker(info, marker)
                                }
                                // 개별 마커(leaf) 업데이트 로직
                                .leafMarkerUpdater { info, marker ->
                                    updateDistrictLeafMarker(info, marker) { binId ->
                                        // 마커 클릭 시 즐겨찾기 토글
                                        binListViewModel.toggleBookmark(binId)
                                    }
                                }
                                .build()
                            mapViewModel.setClustererDistrict(newClustererDistrict)
                        }

                        //북마크 클러스터 초기화
                        if (mapViewModel.clustererBookmarked.value == null) {
                            val newClustererBookmark = Clusterer.Builder<ItemKey>()
                                // 클러스터 마커(집합 마커) 업데이트 로직
                                .clusterMarkerUpdater { info, marker ->
                                    updateBookmarkClusterMarker(info, marker)
                                }
                                // 개별 마커(leaf) 업데이트 로직
                                .leafMarkerUpdater { info, marker ->
                                    updateBookmarkLeafMarker(info, marker)
                                }
                                .build()
                            mapViewModel.setClustererBookmark(newClustererBookmark)
                        }

                        //클러스터를 강제로 다시 그리기 위해 map을 null로 설정
                        mapViewModel.clustererDistrict.value?.map = null
                        mapViewModel.clustererDistrict.value?.map = naverMap

                        mapViewModel.clustererBookmarked.value?.map = null
                        mapViewModel.clustererBookmarked.value?.map = naverMap
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 지도 드래그 상태에 따라 UI 표시 제어
        AnimatedVisibility(
            visible = !isMapInteracting.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            // 드롭다운 메뉴를 통해 상위 지역 선택 후 해당 지역의 상세지역(구/시)를 선택할 수 있도록 함.
            LocationDropdownMenu(
                regionList = Region.entries, // 상위 지역 목록
                supportDistricts = supportDistricts, // 지원하는 상세지역
                selectedRegion = selectedRegion, // 현재 선택된 상위 지역 상태
                selectedDistrict = selectedDistrict, // 현재 선택된 상세지역 상태
                onRegionSelected = { region ->  // 상위 지역이 선택되었을 때 호출되는 콜백
                    // 상위 지역 선택 시 처리할 로직
                    mapViewModel.clearAll()  // 지도 클러스터 초기화 등
                    selectedRegion = region // 선택된 상위 지역 상태 업데이트
                    selectedDistrict = null  // 지역이 변경되면 상세지역 선택은 초기화
                },
                onDistrictSelected = { district ->
                    // 상세지역(구/시) 선택 시 처리할 로직
                    mapViewModel.clearAll()  // 지도 클러스터 초기화 등
                    selectedDistrict = district // 선택된 상세지역 상태 업데이트
                    binListViewModel.onDistrictSelected(district)  // 상세지역에 맞는 초기 데이터를 로드합니다.
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        //더보기 버튼(데이터 있을 때, 전체페이지 아닐 때 출력)
        val shouldShowMoreButton =
            clothingBins.isNotEmpty() && currentPage != totalPage && !isMapInteracting.value
        AnimatedVisibility(
            visible = shouldShowMoreButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp, start = 16.dp, end = 16.dp)
        ) {
            ElevatedButton(
                onClick = {
                    // "더보기" 버튼 클릭 시 데이터 추가 로드
                    binListViewModel.goToNextPage()
                },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
            ) {
                Text(stringResource(R.string.map_more, currentPage, totalPage))
            }
        }

        // 로딩 상태 처리
        if (isLoading) {
            LoadingIndicator()
        }

        // 에러 시 스낵바 출력
        errorMessage?.let { resourceId ->
            val errorMsg = stringResource(resourceId)
            LaunchedEffect(resourceId) {
                showSnackBar(snackbarHostState, errorMsg, SnackbarDuration.Short)
            }
        }

        // 스낵바 호스트 생성
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // selectedDistrict가 변경될 때 호출하여 카메라 포지션 이동
    LaunchedEffect(selectedApiSource) {
        //recomposition으로 재호출 되는 것 방지
        if (previousDistrict.value != selectedApiSource) {
            previousDistrict.value = selectedApiSource
            // 의류수거함이 있으면 첫 번째 위치로 이동
            if (clothingBins.isNotEmpty()) {
                clothingBins.first().longitude?.toDoubleOrNull()?.let { lon ->
                    clothingBins.first().latitude?.toDoubleOrNull()?.let { lat ->
                        naverMapHolder.value?.let { nMap ->
                            animateCameraToPosition(lat, lon, nMap)
                        }
                    }
                }
            }
        }
    }

    // 수거함 데이터 바뀌면 따른 클러스터 업데이트
    LaunchedEffect(clothingBins) {
        //recomposition으로 재호출 되는 것 방지
        if (clothingBins != previousClothingBins.value) {
            previousClothingBins.value = clothingBins

            if (clothingBins.isNotEmpty()) {
                // 아이템 추가
                mapViewModel.addDistrictItems(clothingBins)
            }
        }
    }

    // 북마크 데이터가 변경되었고 지도가 준비된 경우에만 클러스터 업데이트
    LaunchedEffect(bookmarkedBins, naverMapHolder.value) {
        naverMapHolder.value?.let {
            if (bookmarkedBins != previousBookmarkBins.value) {
                previousBookmarkBins.value = bookmarkedBins

                if (bookmarkedBins.isNotEmpty()) {
                    mapViewModel.updateBookmarkedItems(bookmarkedBins, selectedApiSource)
                }
            }
        }
    }

    // 선택 좌표가 변경되었고 지도가 준비된 경우에만 카메라 이동
    LaunchedEffect(selectedCoordinates, naverMapHolder.value) {
        selectedCoordinates?.let { coordinates ->
            naverMapHolder.value?.let { naverMap ->
                animateCameraToPosition(
                    coordinates.latitude,
                    coordinates.longitude,
                    naverMap,
                    FOCUS_ZOOM_LEVEL
                )
                // 좌표 이동 후 값 초기화
                sharedViewModel.clearSelectedCoordinates()
            }
        }
    }

    // 북마크 토글 결과에 따른 스낵바 텍스트 리소스 Id 저장
    LaunchedEffect(Unit) {
        binListViewModel.bookmarkToggleResult.collect { event ->
            bookmarkResultResId = event.messageResId
        }
    }

    //수거함 토글 결과 스낵바 출력
    bookmarkResultResId?.let { resId ->
        val message = stringResource(id = resId)
        LaunchedEffect(resId) {
            showSnackBar(snackbarHostState, message, SnackbarDuration.Short)
            // 메시지 표시 후 상태를 초기화하여 다시 표시되지 않도록 함
            bookmarkResultResId = null
        }
    }

    // NaverMap이 준비되었을 때 권한 요청 처리
    naverMapHolder.value?.let { naverMap ->
        HandlePermissions(context, naverMap, remember {
            FusedLocationSource(context as ComponentActivity, LOCATION_PERMISSION_REQUEST_CODE)
        })
    }
}

// 권한 요청 및 결과 처리
@Composable
fun HandlePermissions(
    context: Context,
    naverMap: NaverMap,
    locationSource: FusedLocationSource
) {

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
        //이미 권한이 있는 경우
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupNaverMapWithLocationTracking(naverMap, locationSource)
        } else { //권한이 없다면 권한 요청
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, // 정확한 위치 권한
                    Manifest.permission.ACCESS_COARSE_LOCATION // 대략적인 위치 권한
                )
            )
        }
    }
}

/**
 * 지역 선택 드롭다운 메뉴
 *
 * 이 메뉴는 두 단계로 구성됨.
 * 1. 상위 지역(예: 서울, 경기도, 경상남도) 선택 모드
 * 2. 선택된 상위 지역에 해당하는 상세지역(구/시) 선택 모드
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdownMenu(
    regionList: List<Region>, // 상위 지역
    supportDistricts: List<ApiSource>, // 지원하는 상세 지역
    selectedRegion: Region?, // 현재 선택된 상위 지역
    selectedDistrict: ApiSource?, // 현재 선택된 상세 지역
    onRegionSelected: (Region) -> Unit, // 상위 지역 선택 시 콜백
    onDistrictSelected: (ApiSource) -> Unit, // 상세 지역 선택 시 콜백
    modifier: Modifier = Modifier
) {
    // 드롭다운 확장 여부
    var expanded by rememberSaveable { mutableStateOf(false) }
    // false: 상위 지역(서울/경기도/경상남도) 선택 모드, true: 상세지역(구/시) 선택 모드
    var inDistrictMode by rememberSaveable { mutableStateOf(false) }

    // 리스트 한글 오름차순으로 정렬
    val context = LocalContext.current
    val sortedRegionList = regionList.sortedBy { context.getString(it.displayNameRes) }

    // 버튼에 표시할 텍스트: 상세지역 선택 시 상세지역, 그렇지 않으면 상위 지역 또는 "지역 선택"
    val displayText = when {
        selectedDistrict != null -> stringResource(selectedDistrict.displayNameRes)
        selectedRegion != null -> stringResource(selectedRegion.displayNameRes)
        else -> stringResource(R.string.select_region)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.padding(top = 24.dp)
    ) {
        ElevatedButton(
            onClick = { },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
            modifier = modifier.menuAnchor()
        ) {
            //드롭다운 버튼
            DropDownButtonComponent(displayText, expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                // 드롭다운이 닫힐 때, 확장 상태와 내부 모드를 초기화
                expanded = false
                inDistrictMode = false
            },
            modifier = Modifier
                .exposedDropdownSize(true)
                .height(250.dp)
        ) {
            if (!inDistrictMode) {
                // 상위 지역 선택 모드
                sortedRegionList.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(stringResource(region.displayNameRes)) },
                        onClick = {
                            // 상위 지역 선택 시 콜백 호출
                            onRegionSelected(region)
                            // 상위 지역 선택 후 상세지역 모드로 전환
                            inDistrictMode = true
                        }
                    )
                }
            } else {
                // 상세지역(구/시) 선택 모드
                // 맨 위에 '뒤로' 버튼 추가하여 상위 지역 선택으로 돌아갈 수 있도록 함.
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.select_previous)) },
                    onClick = { inDistrictMode = false }
                )
                // 선택된 상위 지역에 해당하는 상세지역 리스트 표시
                selectedRegion?.let { region ->
                    supportDistricts.filter {
                        it.region == region
                    }.sortedBy {
                        // 오름차순 정렬
                        context.getString(it.displayNameRes)
                    }.forEach { district ->
                        DropdownMenuItem(
                            // 상세지역 선택 시 콜백 호출
                            text = { Text(stringResource(district.displayNameRes)) },
                            onClick = {
                                onDistrictSelected(district)
                                expanded = false  // 상세지역 선택 후 드롭다운 닫기
                            }
                        )
                    }
                }
            }
        }
    }
}

// 지도 초기화 및 설정 함수
fun setupNaverMap(
    naverMap: NaverMap,
    cameraPosition: CameraPosition?
) {
    //카메라 위치가 있으면 복원, 없으면 기본 위치
    cameraPosition?.let {
        naverMap.cameraPosition = it
    } ?: run {
        naverMap.cameraPosition = CameraPosition(
            NaverMap.DEFAULT_CAMERA_POSITION.target,
            DEFAULT_ZOOM_LEVEL,
        )
    }

    naverMap.uiSettings.apply {
        isLogoClickEnabled = false //로고 클릭 비활성화
        isCompassEnabled = true // 나침반 버튼 활성화
    }
}

//카메라 이동 함수
fun animateCameraToPosition(
    latitude: Double,
    longitude: Double,
    naverMap: NaverMap,
    zoomLevel: Double = DEFAULT_ZOOM_LEVEL
) {
    naverMap.moveCamera(
        CameraUpdate.toCameraPosition(
            CameraPosition(
                LatLng(latitude, longitude),
                zoomLevel
            )
        ).animate(
            CameraAnimation.Easing, NaverMap.DEFAULT_DEFAULT_CAMERA_ANIMATION_DURATION.toLong()
        )
    )
}

// NaverMap 설정 및 위치 추적 모드 설정
fun setupNaverMapWithLocationTracking(
    naverMap: NaverMap,
    locationSource: FusedLocationSource
) {
    naverMap.uiSettings.isLocationButtonEnabled = true // 위치 버튼 활성화
    naverMap.locationSource = locationSource // 위치 소스 설정
}

//맵뷰의 생명주기 관리
@Composable
fun ManageMapViewLifecycle(
    lifecycle: Lifecycle,
    mapView: MapView
) {
    DisposableEffect(lifecycle) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                // MapView가 아직 초기화되지 않은 경우에만 onCreate
                if (mapView.childCount == 0) {
                    mapView.onCreate(null)
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}

private const val DEFAULT_ZOOM_LEVEL = 10.0
private const val FOCUS_ZOOM_LEVEL = 15.0
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000