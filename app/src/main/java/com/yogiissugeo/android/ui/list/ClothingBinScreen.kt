package com.yogiissugeo.android.ui.list

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.ClothingBin

/**
 * 의류 수거함 목록을 표시하는 화면
 *
 * @param navController 네비게이션을 위한 NavHostController
 * @param binListViewModel 의류 수거함 목록을 관리하는 ViewModel
 */
@Composable
fun ClothingBinScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    // ViewModel의 상태 값들을 수집
    val clothingBins by binListViewModel.clothingBins.collectAsState() // 의류 수거함 리스트
    val currentPage by binListViewModel.currentPage.collectAsState() // 현재 페이지 번호
    val isLoading by binListViewModel.isLoading.collectAsState() // 로딩 상태
    val errorMessage by binListViewModel.errorMessage.collectAsState() // 에러 메시지
    val supportDistrict = districtViewModel.districts.collectAsState() // 지원하는 구 목록 가져오기
    val districtList = supportDistrict.value.map { it.displayNameRes }
    val selectedDistrict by binListViewModel.selectedApiSource.collectAsState() // 선택된 구

    val perPage = 100 // 페이지당 항목 수

    // 전체 UI 구조
    Box(modifier = Modifier){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            // 구 선택 드롭다운 메뉴
            DistrictDropdownMenu(
                districtList = districtList,
                selectedDistrict = selectedDistrict?.displayNameRes,
                onDistrictSelected = { selectedName ->
                    val selectedSource = ApiSource.entries.first { it.displayNameRes == selectedName }
                    when(selectedSource){
                        ApiSource.EUNPYEONG, ApiSource.MAPO -> {
                            // CSV 파일을 assets에서 읽어오는 코드
                            selectedSource.csvName?.let { csvName ->
                                val inputStream = context.assets.open(csvName)
                                binListViewModel.loadCsv(inputStream, selectedSource)
                            }
                        }
                        else -> {
                            binListViewModel.onDistrictSelected(selectedSource, perPage)
                        }
                    }
                }
            )

            // 페이지 컨트롤 버튼
            PageControl(
                currentPage = currentPage,
                onPreviousPage = { binListViewModel.goToPreviousPage(perPage) },
                onNextPage = { binListViewModel.goToNextPage(perPage) }
            )

            Spacer(modifier = Modifier.height(8.dp)) // 간격 추가

            // 성공적으로 데이터를 가져왔을 때 LazyColumn으로 목록 렌더링
            ClothingBinList(clothingBins = clothingBins)
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
}


/**
 * 로드 버튼을 표시하는 컴포저블
 *
 * @param onClick 버튼 클릭 시 실행할 함수
 */
@Composable
fun LoadButton(enable: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enable,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.page_load))
    }
}

/**
 * 페이지 네비게이션 버튼을 표시하는 컴포저블
 *
 * @param currentPage 현재 페이지 번호
 * @param onPreviousPage 이전 페이지로 이동하는 함수
 * @param onNextPage 다음 페이지로 이동하는 함수
 */
@Composable
fun PageControl(
    currentPage: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween // 버튼을 양쪽 끝에 배치
    ) {
        // 이전 페이지 버튼
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 // 첫 페이지에서 비활성화
        ) {
            Text(stringResource(R.string.page_control_previous))
        }

        // 현재 페이지 텍스트
        Text(
            text = stringResource(id = R.string.page_control_current, currentPage),
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        // 다음 페이지 버튼
        Button(onClick = onNextPage) {
            Text(stringResource(R.string.page_control_next))
        }
    }
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

/**
 * 의류 수거함 리스트를 표시하는 컴포저블
 *
 * @param clothingBins 의류 수거함 리스트
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun ClothingBinList(clothingBins: List<ClothingBin>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(clothingBins) { bin ->
            BinItem(bin) {
                //TODO 클릭 시 이동
            }
        }
    }
}

/**
 * 의류 수거함 항목을 표시하는 UI 컴포넌트
 *
 * @param bin 의류 수거함 데이터
 * @param onClick 항목 클릭 시 실행될 함수
 */
@Composable
fun BinItem(bin: ClothingBin, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)  // 항목에 패딩 추가
            .clickable { onClick() } // 클릭 시 onClick 실행
    ) {
        // 주소 텍스트
        Text(text = "주소: ${bin.address}")
        // 위도/경도 정보 표시
        if (bin.latitude != null && bin.longitude != null) {
            Text(text = "위도: ${bin.latitude}, 경도: ${bin.longitude}")
        } else {
            Text(text = "위도/경도 정보 없음") // 위도/경도 정보가 없을 때
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
    onDistrictSelected: (Int) -> Unit // 구를 선택했을 때 호출되는 콜백
) {
    // 드롭다운 메뉴 확장 여부를 관리하는 상태
    var expanded by remember { mutableStateOf(false) }

    // 선택된 구 이름을 캐싱하여 중복 호출 방지
    val selectedDistrictText = selectedDistrict?.let { stringResource(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded } // 드롭다운 열기/닫기 토글
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