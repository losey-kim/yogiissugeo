package com.yogiissugeo.android.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource

@Composable
fun BookmarkFilterChip(
    districts: List<Int>,
    selectedDistrict: Int?,
    onDistrictSelected: (ApiSource?) -> Unit,
) {
    // '전체' 옵션
    val allDistrict = R.string.district_all
    // '전체' 옵션을 포함한 전체 지역 리스트
    val districtList = listOf(allDistrict) + districts

    // 가로 스크롤 필터 칩 목록 위한 LazyRow
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp) //칩 간 간격
    ) {
        items(districtList) { districtRes ->
            // 현재 칩이 선택되었는지 여부 확인
            val isSelected = when {
                districtRes == allDistrict -> selectedDistrict == null
                else -> selectedDistrict == districtRes
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    // 칩 클릭 시 필터 선택 로직
                    onDistrictSelected(
                        if (districtRes == allDistrict) null
                        else ApiSource.entries.firstOrNull { it.displayNameRes == districtRes }
                    )
                },
                label = {
                    // 칩 텍스트
                    Text(
                        text = stringResource(id = districtRes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        // 선택된 상태일 때 표시할 아이콘
                        Icon(
                            painter = painterResource(R.drawable.ic_heart_check_fill),
                            contentDescription = "",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null // 선택되지 않은 상태일 때 아이콘 없음
                },
                // 칩의 크기 변경 시 애니메이션 추가
                modifier = Modifier.animateContentSize()
            )
        }
    }
}