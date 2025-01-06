package com.yogiissugeo.android.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yogiissugeo.android.ui.list.BookmarksScreen
import com.yogiissugeo.android.ui.list.SharedMapViewModel
import com.yogiissugeo.android.ui.map.NaverMapScreen
import com.yogiissugeo.android.ui.setting.SettingScreen

/**
 * 앱 내 내비게이션을 관리하는 컴포저블 함수
 *
 * @param navController 네비게이션을 위한 NavHostController
 */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val sharedViewModel: SharedMapViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Map.route, // 앱 시작 시 표시할 기본 경로 설정
        modifier = modifier
    ) {
        //지도화면
        composable(NavigationItem.Map.route) { NaverMapScreen(sharedViewModel = sharedViewModel) }
        //저장된 항목 화면
        composable(NavigationItem.Saved.route) { BookmarksScreen(sharedViewModel = sharedViewModel, navController = navController) }
        //설정화면
        composable(NavigationItem.Setting.route) { SettingScreen() }
    }
}