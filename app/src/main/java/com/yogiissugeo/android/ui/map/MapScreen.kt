package com.yogiissugeo.android.ui.map

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.ui.component.ErrorMessage
import com.yogiissugeo.android.ui.component.LoadingIndicator
import com.yogiissugeo.android.ui.list.BinListViewModel
import com.yogiissugeo.android.ui.list.DistrictViewModel
import com.yogiissugeo.android.utils.common.loadCsvFromAssets

@Composable
fun NaverMapScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴

    //페이지 관련
    val perPage = 100 // 페이지당 항목 수

    // 의류수거함 관련 상태
    val clothingBins = binListViewModel.clothingBins.collectAsState().value
    val supportDistricts = districtViewModel.districts.collectAsState() // 지원하는 구 목록 가져오기
    val isLoading by binListViewModel.isLoading.collectAsState()
    val errorMessage by binListViewModel.errorMessage.collectAsState()
    val selectedDistrict by binListViewModel.selectedApiSource.collectAsState()
    val currentPage by binListViewModel.currentPage.collectAsState()
    val totalPage by binListViewModel.totalPage.collectAsState()

    // 지도 관련 상태
    val mapView = mapViewModel.mapView
    val naverMapState by mapViewModel.naverMapState.collectAsState()
    val clusterer = mapViewModel.clusterer.collectAsState()
    val keyTagMap = mapViewModel.keyTagMap.collectAsState()

    // 로컬 상태
    val isMapInteracting = rememberSaveable { mutableStateOf(false) } // 지도 조작 상태
    val previousClothingBins = rememberSaveable { mutableStateOf(clothingBins) }  //의류수거함 값
    val previousDistrict = rememberSaveable { mutableStateOf(selectedDistrict) }

    // 위치 권한
    val locationSource = remember {
        FusedLocationSource(context as ComponentActivity, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // MapView의 Lifecycle 관리
    ManageMapViewLifecycle(lifecycle, mapView, naverMapState)

    Box(modifier = Modifier.fillMaxSize()) {
        // AndroidView로 MapView를 Compose UI에 포함
        NaverMapContainer(mapView, mapViewModel, isMapInteracting)

        // 지도 드래그 상태에 따라 UI 표시 제어
        AnimatedVisibility(
            visible = !isMapInteracting.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ){
            // 구 선택 드롭다운 메뉴
            DistrictDropdownMenu(
                districtList = supportDistricts.value.map { it.displayNameRes },
                selectedDistrict = selectedDistrict?.displayNameRes,
                onDistrictSelected = { selectedName ->
                    //구 선택 시 클러스터 초기화
                    mapViewModel.setKeyTagMap(null)
                    val selectedSource = ApiSource.entries.first { it.displayNameRes == selectedName }
                    if (selectedSource in ApiSource.CSV_SOURCES) { //노원구, 은평구, 마포구, 중구
                        // CSV 파일을 assets에서 읽어오는 코드
                        selectedSource.csvName?.let { csvName ->
                            context.loadCsvFromAssets(csvName, { inputStream ->
                                // 파일을 성공적으로 읽어왔을 경우 ViewModel에 데이터를 저장
                                binListViewModel.loadCsv(inputStream, selectedSource)
                            }) {
                                //파일 로드 실패 시
                                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else { //이외의 구는 API요청
                        binListViewModel.onDistrictSelected(selectedSource, perPage)
                    }
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
        ){
            ElevatedButton(
                onClick = {
                    // "더보기" 버튼 클릭 시 현재 좌표 전달
                    binListViewModel.goToNextPage(perPage)
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
        if (previousDistrict.value != selectedDistrict){
            previousDistrict.value = selectedDistrict
            if (clothingBins.isNotEmpty()) {
                clothingBins.first().longitude?.let { longitude ->
                    clothingBins.first().latitude?.let { latitude ->
                        naverMapState?.let { naverMap ->
                            animateCameraToPosition(latitude.toDouble(), longitude.toDouble(), naverMap)
                        }
                    }
                }
            }
        }
    }

    // 수거함 데이터 변경에 따른 클러스터 업데이트
    LaunchedEffect(clothingBins) {
        //recomposition으로 재호출 되는 것 방지
        if (clothingBins != previousClothingBins.value) {
            previousClothingBins.value = clothingBins
            //클러스터
            naverMapState?.let { naverMap ->
                if (clothingBins.isNotEmpty()) {
                    //클러스터 설정
                    addCluster(
                        clusterer.value,
                        keyTagMap.value,
                        naverMap,
                        clothingBins,
                        onMarkerClick = { binId -> binListViewModel.toggleBookmark(binId) } // 콜백 전달
                    ) { newclusterer, keyTagMap ->
                        mapViewModel.setClusterer(newclusterer)
                        mapViewModel.setKeyTagMap(keyTagMap)
                    }
                }
            }
        }
    }

    // NaverMap이 준비되었을 때 권한 요청 처리
    naverMapState?.let { naverMap ->
        HandlePermissions(context, naverMap, locationSource) // NaverMap을 전달
    }
}

//네이버 지도 View
@Composable
fun NaverMapContainer(
    mapView: MapView,
    mapViewModel: MapViewModel,
    isMapInteracting: MutableState<Boolean>
) {
    AndroidView(
        factory = { mapView }, // MapView를 생성
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        // MapView가 준비되었을 때 호출되는 콜백
        mapView.getMapAsync { naverMap ->
            if (mapViewModel.naverMapState.value == null) {
                mapViewModel.setNaverMapState(naverMap) // NaverMap 상태 업데이트
                setupNaverMap(naverMap) // NaverMap 설정

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
                }
            }
        }
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
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupNaverMapWithLocationTracking(naverMap, locationSource)
        } else { //권한이 없다면 권한 요청
            launcher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, // 정확한 위치 권한
                    android.Manifest.permission.ACCESS_COARSE_LOCATION // 대략적인 위치 권한
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
    val selectedDistrictText =
        selectedDistrict?.let { stringResource(it) } ?: stringResource(R.string.select_district)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 선택된 구 텍스트
                Text(text = selectedDistrictText)

                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

                // 드롭다운 아이콘
                Icon(
                    painter = painterResource(if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null
                )
            }
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize(true)
                .height(200.dp)
        ) {
            districtList.forEach { district ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(district)) },
                    onClick = {
                        onDistrictSelected(district)
                        expanded = false // 항목 클릭 시 드롭다운 닫기
                    },
                )
            }
        }
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000