package com.yogiissugeo.android.ui.nav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yogiissugeo.android.utils.navigation.navigateWithOptions


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // BottomNavigationBar에 표시할 항목 정의
    val items = listOf(
        NavigationItem.Map, // 지도 화면 항목
        NavigationItem.Saved, // 저장된 항목 화면
        NavigationItem.Setting // 설정 화면 항목
    )

    // BottomNavigationBar UI 구성
    NavigationBar {
        // 현재 네비게이션 경로를 추적하여 선택 상태를 결정
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route, // 현재 경로와 항목의 경로가 일치하면 선택 상태
                icon = {
                    Icon(
                        // 선택 상태에 따라 다른 아이콘 표시
                        painter = painterResource(if (currentRoute == item.route) item.selectedIcon else item.icon),
                        contentDescription = stringResource(item.title)
                    )
                },
                label = { Text(stringResource(item.title)) },
                onClick = {
                    // 항목 클릭 시 해당 화면으로 이동
                    navController.navigateWithOptions(item.route)
                }
            )
        }
    }
}