package com.yogiissugeo.android.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.naver.maps.geometry.LatLng
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.ui.component.BookmarkFilterChip
import com.yogiissugeo.android.ui.component.LoadingIndicator
import com.yogiissugeo.android.ui.component.showSnackBar
import com.yogiissugeo.android.ui.nav.NavigationItem
import com.yogiissugeo.android.utils.navigation.navigateWithOptions


//저장된 화면
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(
    binListViewModel: BinListViewModel = hiltViewModel(),
    districtViewModel: DistrictViewModel = hiltViewModel(),
    sharedViewModel: SharedMapViewModel = hiltViewModel(),
    savedCardViewModel: SavedCardViewModel = hiltViewModel(),
    navController: NavHostController
) {
    // 구 목록 가져오기
    val districts by districtViewModel.districts.collectAsState()

    // 선택된 구 상태를 ViewModel과 연동
    val selectedDistrict by binListViewModel.selectedApiSource.collectAsState()

    // 현재 선택된 구 필터로 북마크된 목록
    val bookmarkBins = binListViewModel.bookmarksBins.collectAsLazyPagingItems()

    // 현재 필터링된 결과의 갯수 관찰
    val bookmarkCount by binListViewModel.bookmarkCount.collectAsState()

    // SnackbarHostState 생성
    val snackbarHostState = remember { SnackbarHostState() }
    // 북마크 토글 결과 리소스 아이디
    var bookmarkResultResId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // 구 필터
                BookmarkFilterChip(
                    districts = districts.map { it.displayNameRes },
                    selectedDistrict = selectedDistrict?.displayNameRes,
                    onDistrictSelected = { district ->
                        //구 선택 시 데이터 로드
                        binListViewModel.setSelectedApiSource(district)
                    }
                )
            }

            //상단 고정
            stickyHeader {
                //총 갯수와 정렬기준 출력
                SetInformation(bookmarkCount)
            }

            // 데이터가 비어 있을 경우
            if (bookmarkBins.itemCount == 0) {
                item { EmptySavedList() }
            } else {
                // 데이터가 있을 경우 리스트로 표시
                items(
                    bookmarkBins.itemCount,
                    key = { index -> bookmarkBins[index]?.id ?: "" }) { index ->
                    val bin = bookmarkBins[index] // LazyPagingItems에서 항목을 가져옴
                    bin?.let {
                        SavedCard(
                            bin = it,
                            onCardClick = { latLang ->
                                // 카드 클릭 시 좌표 저장
                                sharedViewModel.selectCoordinates(latLang)
                                // 지도로 이동
                                navController.navigateWithOptions(NavigationItem.Map.route)
                            },
                            onToggleBookmark = { binId ->
                                //북마크 제거 로직
                                binListViewModel.toggleBookmark(binId)
                            },
                            onOpenApp = { latLang, address ->
                                //앱 열기 버튼
                                savedCardViewModel.onNavigate(latLang, address)
                            },
                            onShareAddress = { address ->
                                //공유 버튼
                                savedCardViewModel.onShareAddress(address)
                            },
                            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                        )
                    }
                }

                // 로드 실패 시 재시도 버튼
                if (bookmarkBins.loadState.append is LoadState.Error) {
                    item {
                        RetryButton(
                            onClick = { bookmarkBins.retry() },
                        )
                    }
                }
            }
        }

        // 스낵바 호스트 생성
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    //로드 상태에 따른 UI
    ShowLoadState(bookmarkBins.loadState){
        // 로드 재시도
        bookmarkBins.retry()
    }

    // 북마크 추가 여부에 따른 스낵바 텍스트 리소스 Id 저장
    LaunchedEffect(Unit) {
        binListViewModel.bookmarkToggleResult.collect { event ->
            bookmarkResultResId = event.messageResId
        }
    }

    //수거함 토글 결고 스낵바 출력
    bookmarkResultResId?.let { resId ->
        val message = stringResource(id = resId)
        LaunchedEffect(resId) {
            showSnackBar(snackbarHostState, message, SnackbarDuration.Short)
            // 메시지 표시 후 상태를 초기화하여 다시 표시되지 않도록 함
            bookmarkResultResId = null
        }
    }
}

/**
 * 총 갯수와 정렬 기준 출력
 */
@Composable
fun SetInformation(bookmarkCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // 결과 갯수 표시
            Text(
                text = stringResource(R.string.bookmarks_get_count, bookmarkCount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // 최근 저장 순 표시
            Text(
                text = stringResource(R.string.bookmarks_sort_recently),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        //구분선
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    Spacer(modifier = Modifier.height(4.dp))
}

/**
 * 개별 저장 항목을 표시
 */
@Composable
fun SavedCard(
    bin: ClothingBin,
    onCardClick: (LatLng) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShareAddress: (String) -> Unit,
    onOpenApp: (LatLng, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val latLng = LatLng((bin.latitude?.toDouble() ?: 0.0), (bin.longitude?.toDouble() ?: 0.0))
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(
                // 카드 클릭 시 지도로 이동
                onClick = { onCardClick(latLng) }
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_card)),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 텍스트 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // 구 텍스트
                Text(
                    text = stringResource(ApiSource.entries.first { it.name == bin.district }.displayNameRes),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 주소 텍스트
                Text(
                    text = bin.address.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 버튼 영역
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 네이버 지도 이동 버튼
                IconButton(
                    onClick = { onOpenApp(latLng, bin.address.toString()) },
                    modifier = Modifier.size(24.dp)
                ) {
                    AsyncImage(
                        model = R.drawable.ic_naver_map, // 네이버 로고 리소스
                        contentDescription = stringResource(R.string.bookmarks_open_app),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 주소 공유 버튼
                IconButton(
                    onClick = { onShareAddress(bin.address.toString()) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = stringResource(R.string.bookmarks_share),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // 북마크 제거 버튼
                IconButton(
                    onClick = { onToggleBookmark(bin.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart_minus_fill),
                        contentDescription = stringResource(R.string.bookmarks_remove),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 즐겨찾기 목록이 비어 있을 때 표시되는 메시지 UI.
 *
 */
@Composable
fun EmptySavedList() {
    // 즐겨찾기 목록이 비어 있을 때 메시지 표시
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            //아이콘
            Image(
                painter = painterResource(R.drawable.watste_basket),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))

            //타이틀
            Text(
                text = stringResource(id = R.string.bookmarks_empty_error),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            //설명
            Text(
                text = stringResource(id = R.string.bookmarks_empty_description),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * 로드 상태에 따른 UI
 */
@Composable
fun ShowLoadState(loadState: CombinedLoadStates, onRetry: () -> Unit) {
    when {
        // 로드 중
        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
            LoadingIndicator()
        }

        // 로드 실패
        loadState.refresh is LoadState.Error -> {
            ErrorMessage({
                onRetry()
            })
        }
    }
}

/**
 * 로드 실패 시 데이터를 다시 시도할 수 있는 버튼 UI.
 */
@Composable
fun RetryButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick() }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = null,
        )
    }
}

/**
 * 오류 메시지 및 재시도 버튼
 */
@Composable
fun ErrorMessage(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //에러 메시지 출력
        Text(
            text = stringResource(R.string.error_unknown),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        //재시도 버튼
        RetryButton(onClick)
    }
}

@Preview(showBackground = true)
@Composable
fun showRetryButton() {
    RetryButton({})
}

@Preview(showBackground = true)
@Composable
fun showSetInformationPreview() {
    SetInformation(100)
}

@Preview(showBackground = true)
@Composable
fun SavedCardPreview() {
    SavedCard(ClothingBin(id = "", "라라라라라", district = "마포구"), {}, {}, {}, { _, _ -> }, Modifier)
}

@Preview(showBackground = true)
@Composable
fun SavedScreenPreview() {
    EmptySavedList()
}