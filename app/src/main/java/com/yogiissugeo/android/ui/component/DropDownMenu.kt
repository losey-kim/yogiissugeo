package com.yogiissugeo.android.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.yogiissugeo.android.R

/**
 * 드롭다운 버튼 컴포넌트
 *
 * @param title 버튼 텍스트
 * @param expanded 버튼 펼침 여부
 */
@Composable
fun DropDownButtonComponent(title: String, expanded: Boolean) {
    //텍스트
    Text(text = title)

    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

    // 드롭다운 아이콘
    Icon(
        painter = painterResource(if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
        contentDescription = ""
    )
}

/**
 * 드롭다운 메뉴 컴포넌트
 *
 * @param list 메뉴 리스트
 * @param expanded 메뉴 펼침 여부
 * @param modifier 메뉴 modifier
 * @param onDismissRequest 메뉴 닫기 콜백
 * @param onMenuSelected 메뉴 선택 콜백
 */
@Composable
fun DropDownMenuComponent(
    list: List<Int>,
    expanded: Boolean,
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    onMenuSelected: (Int) -> Unit,
) {
    // 드롭다운 메뉴
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            //메뉴 닫음
            onDismissRequest()
        },
        modifier = modifier
    ) {
        //리스트에 있는 아이템 메뉴화
        list.forEach { district ->
            DropdownMenuItem(
                text = { Text(text = stringResource(district)) },
                onClick = {
                    //메뉴 선택
                    onMenuSelected(district)
                },
            )
        }
    }
}