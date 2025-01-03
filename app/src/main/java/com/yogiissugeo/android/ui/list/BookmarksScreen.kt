package com.yogiissugeo.android.ui.list

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.ui.component.LoadingIndicator

//저장된 화면
@Composable
fun BookmarksScreen(binListViewModel: BinListViewModel = hiltViewModel()) {
    val bookmarkBins = binListViewModel.bookmarksBins.collectAsLazyPagingItems()

    // 데이터가 비어 있을 경우
    if (bookmarkBins.itemCount == 0) {
        when (bookmarkBins.loadState.refresh) {
            is LoadState.Loading -> LoadingIndicator() // 로딩 중
            is LoadState.NotLoading -> EmptySavedList() // 데이터 없음
            is LoadState.Error -> ErrorMessage((bookmarkBins.loadState.refresh as LoadState.Error).error) // 에러 발생
        }
    } else {
        // 데이터가 있을 경우 리스트로 표시
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
            ,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // 항목 사이 간격 추가
        ) {
            // 저장 항목을 리스트에 추가
            items(bookmarkBins.itemCount, key = { index -> bookmarkBins[index]?.id ?: "" }) { index ->
                val bin = bookmarkBins[index] // LazyPagingItems에서 항목을 가져옴
                bin?.let {
                    SavedCard(
                        bin = it,
                        onToggleBookmark = { binId -> binListViewModel.toggleBookmark(binId) },
                        Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
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
 * 개별 저장 항목을 표시
 */
@Composable
fun SavedCard(bin: ClothingBin, onToggleBookmark: (String) -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                //TODO 카드 클릭 시 이동 추가
                true
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 텍스트 영역
            Text(
                text = bin.address.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), // 텍스트 영역 가중치 설정
                maxLines = 2, // 텍스트 줄 수 제한
                overflow = TextOverflow.Ellipsis // 텍스트가 길면 줄임표 처리
            )

            // 북마크 버튼
            IconButton(
                onClick = { onToggleBookmark(bin.id) },
                modifier = Modifier.padding(start = 8.dp)
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
        Text(text = stringResource(R.string.bookmarks_error_message, errorMessage.toString()), color = MaterialTheme.colorScheme.error)
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
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SavedScreenPreview() {
    EmptySavedList()
}

@Preview(showBackground = true)
@Composable
fun SavedCardPreview() {
    SavedCard(ClothingBin(id = "", "라라라라라"), {}, Modifier)
}