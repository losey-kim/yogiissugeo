package com.personal.yogiissugeo.ui.list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.personal.yogiissugeo.data.model.ClothingBin

/**
 * 의류 수거함 목록을 표시하는 화면
 *
 * @param navController 네비게이션을 위한 NavHostController
 * @param binListViewModel 의류 수거함 목록을 관리하는 ViewModel
 */
@Composable
fun ClothingBinScreen(
    navController: NavHostController,
    binListViewModel: BinListViewModel = hiltViewModel()
) {
    // ViewModel의 상태 값들을 수집
    val clothingBins by binListViewModel.clothingBins.collectAsState() // 의류 수거함 리스트
    val currentPage by binListViewModel.currentPage.collectAsState() // 현재 페이지 번호
    val isLoading by binListViewModel.isLoading.collectAsState() // 로딩 상태
    val errorMessage by binListViewModel.errorMessage.collectAsState() // 에러 메시지

    val perPage = 3 // 페이지당 항목 수 (고정값)

    // 전체 UI 구조
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 로드 버튼: 데이터를 로드하는 버튼
        LoadButton(
            onClick = { binListViewModel.loadClothingBins(currentPage, perPage) }
        )

        Spacer(modifier = Modifier.height(8.dp)) // 간격 추가

        // 페이지 네비게이션 버튼
        PageControl(
            currentPage = currentPage,
            onPreviousPage = { binListViewModel.goToPreviousPage(perPage) },
            onNextPage = { binListViewModel.goToNextPage(perPage) }
        )

        Spacer(modifier = Modifier.height(8.dp)) // 간격 추가

        // 로딩 상태: 로딩 중일 때 로딩 인디케이터를 표시
        if (isLoading) {
            LoadingIndicator()
            return // 로딩 중일 때는 UI를 종료하고 로딩만 표시
        }

        // 에러 상태: 에러 메시지가 있으면 에러 메시지를 표시
        errorMessage?.let { resourceId ->
            ErrorMessage(resourceId = resourceId)
            return // 에러가 발생한 경우 UI 종료
        }

        // 성공적으로 데이터를 가져왔을 때 LazyColumn으로 목록 렌더링
        ClothingBinList(clothingBins = clothingBins, navController = navController)
    }
}

/**
 * 로드 버튼을 표시하는 컴포저블
 *
 * @param onClick 버튼 클릭 시 실행할 함수
 */
@Composable
fun LoadButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("로드")
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
            Text("이전 페이지")
        }

        // 현재 페이지 텍스트
        Text(text = "페이지: $currentPage", modifier = Modifier.align(Alignment.CenterVertically))

        // 다음 페이지 버튼
        Button(onClick = onNextPage) {
            Text("다음 페이지")
        }
    }
}

/**
 * 로딩 인디케이터를 표시하는 컴포저블
 */
@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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
        Text(text = stringResource(id = resourceId), color = Color.Red)
    }
}

/**
 * 의류 수거함 리스트를 표시하는 컴포저블
 *
 * @param clothingBins 의류 수거함 리스트
 * @param navController 네비게이션을 위한 NavHostController
 */
@Composable
fun ClothingBinList(clothingBins: List<ClothingBin>, navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(clothingBins) { bin ->
            BinItem(bin) {
                val latitude = bin.latitude
                val longitude = bin.longitude
                if (latitude != null && longitude != null) {
                    navController.navigate("mapScreen/$latitude/$longitude")
                }
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
        // 행정동 텍스트
        Text(text = "행정동: ${bin.district}")
        // 위도/경도 정보 표시
        if (bin.latitude != null && bin.longitude != null) {
            Text(text = "위도: ${bin.latitude}, 경도: ${bin.longitude}")
        } else {
            Text(text = "위도/경도 정보 없음") // 위도/경도 정보가 없을 때
        }
    }
}