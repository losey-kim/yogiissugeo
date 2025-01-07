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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.naver.maps.geometry.LatLng
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.ui.component.BookmarkFilterChip
import com.yogiissugeo.android.ui.component.LoadingIndicator
import com.yogiissugeo.android.ui.nav.NavigationItem


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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(vertical = 16.dp),
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
            when (bookmarkBins.loadState.refresh) {
                is LoadState.Loading -> {// 로딩 중
                    item { LoadingIndicator() }
                }

                is LoadState.NotLoading -> {// 데이터 없음
                    item { EmptySavedList() }
                }

                is LoadState.Error -> { // 에러 발생
                    item { ErrorMessage((bookmarkBins.loadState.refresh as LoadState.Error).error) }
                }
            }
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
                            navController.navigate(NavigationItem.Map.route)
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

            // 추가 상태 처리
            bookmarkBins.apply {
                when {
                    // 로딩 상태
                    loadState.refresh is LoadState.Loading -> {
                        item { LoadingIndicator() }
                    }
                    // 오류 상태
                    loadState.refresh is LoadState.Error -> {
                        val e = loadState.refresh as LoadState.Error
                        item { ErrorMessage(error = e.error) }
                    }
                    // 추가 데이터 로드 중
                    loadState.append is LoadState.Loading -> {
                        item { LoadingIndicator() }
                    }
                    // 추가 데이터 로드 오류 처리
                    loadState.append is LoadState.Error -> {
                        val e = loadState.append as LoadState.Error
                        item {
                            RetryButton(
                                onClick = { bookmarkBins.retry() },
                                errorMessage = e.error.localizedMessage
                            )
                        }
                    }
                }
            }
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
            .background(Color.White)
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
 * 로드 실패 시 데이터를 다시 시도할 수 있는 버튼 UI.
 * @param errorMessage 오류 메시지를 표시
 */
@Composable
fun RetryButton(onClick: () -> Unit, errorMessage: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.bookmarks_error_message, errorMessage.toString()),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClick) {
            Text(text = stringResource(R.string.bookmarks_retry_load))
        }
    }
}

/**
 * 오류 메시지를 표시하는 Composable 함수.
 */
@Composable
fun ErrorMessage(error: Throwable) {
    Text(
        text = stringResource(R.string.bookmarks_error_message, error.toString()),
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
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