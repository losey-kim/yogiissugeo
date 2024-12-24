package com.personal.yogiissugeo.ui.map

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.personal.yogiissugeo.R
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.ui.list.BinListViewModel
import com.personal.yogiissugeo.ui.list.DistrictViewModel
import com.personal.yogiissugeo.utils.common.loadCsvFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

@Composable
fun NaverMapScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current // 현재 Compose가 실행되는 Context를 가져옴
    val lifecycle = LocalLifecycleOwner.current.lifecycle // 현재 LifecycleOwner를 가져옴

    //의류수거함 값
    val clothingBins = binListViewModel.clothingBins.collectAsState().value
    val currentPage by binListViewModel.currentPage.collectAsState() // 현재 페이지 번호
    val isLoading by binListViewModel.isLoading.collectAsState() // 로딩 상태
    val errorMessage by binListViewModel.errorMessage.collectAsState() // 에러 메시지
    val supportDistricts = districtViewModel.districts.collectAsState() // 지원하는 구 목록 가져오기
    val selectedDistrict by binListViewModel.selectedApiSource.collectAsState() // 선택된 구

    //지도 관련
    val mapView = mapViewModel.mapView
    val naverMapState by mapViewModel.naverMapState.collectAsState()
    val clusterer = mapViewModel.clusterer.collectAsState()
    val keyTagMap = mapViewModel.keyTagMap.collectAsState()
    val perPage = 100 // 페이지당 항목 수


    // 위치 권한
    val locationSource = remember {
        FusedLocationSource(context as ComponentActivity, LOCATION_PERMISSION_REQUEST_CODE)
    }

    // MapView의 Lifecycle 관리
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
                if (naverMapState == null) {
                    mapViewModel.setNaverMapState(naverMap) // NaverMap 상태 업데이트
                    setupNaverMap(naverMap, selectedDistrict) // NaverMap 설정
                }
            }
        }

        // 구 선택 드롭다운 메뉴
        DistrictDropdownMenu(
            districtList = supportDistricts.value.map { it.displayNameRes },
            selectedDistrict = selectedDistrict?.displayNameRes,
            onDistrictSelected = { selectedName ->
                //구 선택 시 클러스터 초기화
                mapViewModel.setKeyTagMap(null)
                val selectedSource = ApiSource.entries.first { it.displayNameRes == selectedName }
                when (selectedSource) {
                    ApiSource.EUNPYEONG, ApiSource.MAPO -> { //은평, 마포구
                        // CSV 파일을 assets에서 읽어오는 코드
                        selectedSource.csvName?.let { csvName ->
                            context.loadCsvFromAssets(csvName, { inputStream ->
                                // 파일을 성공적으로 읽어왔을 경우 ViewModel에 데이터를 저장
                                binListViewModel.loadCsv(inputStream, selectedSource)
                            }) {
                                //파일 로드 실패 시
                                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> { //이외의 구는 API요청
                        binListViewModel.onDistrictSelected(selectedSource, perPage)
                    }
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        //더보기 버튼(로딩 및 에러아닐 때 출력)
        if (!isLoading && errorMessage == null) {
            Button(
                onClick = {
                    // "더보기" 버튼 클릭 시 현재 좌표 전달
                    binListViewModel.goToNextPage(perPage)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(stringResource(R.string.map_more))
            }
        }

        // 로딩 상태 처리
        if (isLoading) {
            LoadingIndicator()
        }

        // 에러 상태 처리
        errorMessage?.let { resourceId ->
            ErrorMessage(resourceId = resourceId)
        }
    }

    // selectedDistrict가 변경될 때 호출하여 카메라 포지션 이동
    LaunchedEffect(selectedDistrict) {
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

    // 수거함 데이터 변경에 따른 클러스터 업데이트
    LaunchedEffect(clothingBins) {
        //클러스터
        naverMapState?.let { naverMap ->
            if (clothingBins.isNotEmpty()) {
                //클러스터 설정
                addCluster(
                    clusterer.value,
                    keyTagMap.value,
                    naverMap,
                    clothingBins
                ) { newclusterer, keyTagMap ->
                    mapViewModel.setClusterer(newclusterer)
                    mapViewModel.setKeyTagMap(keyTagMap)
                }
            }
        }
    }

    // NaverMap이 준비되었을 때 권한 요청 처리
    naverMapState?.let { naverMap ->
        HandlePermissions(naverMap, locationSource) // NaverMap을 전달
    }
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
        //이미 권한이 있는 경우
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
 * @param selectedDistrict 현재 선택된 구의 리소스 ID. 선택되지 않은 경우 null.
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
    val selectedDistrictText = selectedDistrict?.let { stringResource(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }, // 드롭다운 열기/닫기 토글
        modifier = modifier
    ) {
        // 선택된 구를 표시하는 텍스트 필드
        OutlinedTextField(
            value = selectedDistrictText, // 선택된 구 이름 표시
            onValueChange = {},
            readOnly = true, // 읽기 전용
            label = { Text(stringResource(R.string.select_district)) }, // 레이블 설정
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.menuAnchor() // 메뉴와 텍스트 필드를 연결
        )

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false } // 메뉴 외부를 클릭하면 닫힘
        ) {
            // 구 이름 리스트를 순회하며 항목 생성
            districtList.forEach { district ->
                DistrictDropdownMenuItem(
                    districtResId = district, // 구 이름 리소스 ID 전달
                    onSelected = {
                        onDistrictSelected(it) // 선택된 구 ID를 콜백으로 반환
                        expanded = false // 선택 후 드롭다운 닫기
                    }
                )
            }
        }
    }
}

/**
 * 드롭다운 메뉴의 개별 항목을 표시하는 컴포저블.
 *
 * @param districtResId 구 이름의 리소스 ID.
 * @param onSelected 항목이 선택되었을 때 호출되는 콜백. 선택된 구의 리소스 ID를 반환.
 */
@Composable
fun DistrictDropdownMenuItem(
    @StringRes districtResId: Int, // 구 이름의 리소스 ID
    onSelected: (Int) -> Unit // 선택된 구의 리소스 ID를 반환하는 콜백
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(districtResId)) }, // 구 이름 표시
        onClick = { onSelected(districtResId) } // 클릭 시 콜백 호출
    )
}

/**
 * 로딩 인디케이터를 표시하는 컴포저블
 */
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * 에러 메시지를 표시하는 컴포저블
 *
 * @param resourceId 에러 메시지를 표시할 리소스 ID
 */
@Composable
fun ErrorMessage(resourceId: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(id = resourceId),
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000