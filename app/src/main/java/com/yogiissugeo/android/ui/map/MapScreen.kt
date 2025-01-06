package com.yogiissugeo.android.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
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
import com.naver.maps.map.util.FusedLocationSource
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.ui.component.ErrorMessage
import com.yogiissugeo.android.ui.component.LoadingIndicator
import com.yogiissugeo.android.ui.component.DropDownMenuComponent
import com.yogiissugeo.android.ui.component.DropDownButtonComponent
import com.yogiissugeo.android.ui.list.BinListViewModel
import com.yogiissugeo.android.ui.list.DistrictViewModel

@Composable
fun NaverMapScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴

    // 의류수거함 관련 상태
    val clothingBins by binListViewModel.clothingBins.collectAsState()
    val bookmarkedBins by binListViewModel.allBookmarkedBins.collectAsState(emptyList())
    val supportDistricts by districtViewModel.districts.collectAsState() // 지원하는 구 목록 가져오기
    val isLoading by binListViewModel.isLoading.collectAsState()
    val errorMessage by binListViewModel.errorMessage.collectAsState()
    val selectedDistrict by binListViewModel.selectedApiSource.collectAsState()
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
    val previousDistrict = rememberSaveable { mutableStateOf(selectedDistrict) }

    // MapView의 Lifecycle 관리
    ManageMapViewLifecycle(lifecycle, mapView)

    Box(modifier = Modifier.fillMaxSize()) {
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

                        // 클러스터러가 없으면 초기화 (한 번만)
                        mapViewModel.initClustererIfNeeded { binId ->
                            // 마커 클릭 시 BinListViewModel에 북마크 토글.
                            binListViewModel.toggleBookmark(binId)
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
            // 구 선택 드롭다운 메뉴
            DistrictDropdownMenu(
                districtList = supportDistricts.map { it.displayNameRes },
                selectedDistrict = selectedDistrict?.displayNameRes,
                onDistrictSelected = { selectedName ->
                    //구 선택 시 클러스터 초기화
                    mapViewModel.clearAll()
                    val selectedSource = ApiSource.entries.first { it.displayNameRes == selectedName }
                    //초기 데이터 로드
                    binListViewModel.onDistrictSelected(selectedSource)
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        //더보기 버튼(데이터 있을 때, 전체페이지 아닐 때 출력)
        val shouldShowMoreButton = clothingBins.isNotEmpty() && currentPage != totalPage && !isMapInteracting.value
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

        // 에러 시 토스트메시지 출력
        errorMessage?.let { resourceId ->
            ErrorMessage(context, resourceId)
        }
    }

    // selectedDistrict가 변경될 때 호출하여 카메라 포지션 이동
    LaunchedEffect(selectedDistrict) {
        //recomposition으로 재호출 되는 것 방지
        if (previousDistrict.value != selectedDistrict) {
            previousDistrict.value = selectedDistrict
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
        val map = naverMapHolder.value
        if (map != null && bookmarkedBins != previousBookmarkBins.value){
            previousBookmarkBins.value = bookmarkedBins

            if (bookmarkedBins.isNotEmpty()){
                mapViewModel.updateBookmarkedItems(bookmarkedBins, selectedDistrict)
            }
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
 * 구 선택을 위한 드롭다운 메뉴 컴포저블.
 *
 * @param districtList 구 이름의 리소스 ID 리스트.
 * @param selectedDistrict 현재 선택된 구의 리소스 ID. 선택되지 않은 경우 빈 값.
 * @param onDistrictSelected 사용자가 구를 선택했을 때 호출되는 콜백. 선택된 구의 리소스 ID를 반환.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistrictDropdownMenu(
    districtList: List<Int>, // 드롭다운에 표시할 구 이름의 리소스 ID 리스트
    @StringRes selectedDistrict: Int?, // 현재 선택된 구의 리소스 ID
    onDistrictSelected: (Int) -> Unit, // 구를 선택했을 때 호출되는 콜백
    modifier: Modifier
) {
    // 드롭다운 메뉴 확장 여부를 관리하는 상태
    var expanded by rememberSaveable { mutableStateOf(false) }

    // 선택된 구 이름
    val selectedDistrictText = stringResource(selectedDistrict ?: R.string.select_district)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.padding(top = 32.dp)
    ) {
        ElevatedButton(
            onClick = {
            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
            modifier = modifier
                .menuAnchor()
        ) {
            //드롭다운 버튼
            DropDownButtonComponent(selectedDistrictText, expanded)
        }

        // 드롭다운 메뉴
        DropDownMenuComponent(
            list = districtList,
            expanded = expanded,
            modifier = Modifier
                .exposedDropdownSize(true)
                .height(200.dp),
            onDismissRequest = { expanded = false },
            onMenuSelected = { district ->
                //항목 클릭
                onDistrictSelected(district)
                expanded = false
            }
        )
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
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000