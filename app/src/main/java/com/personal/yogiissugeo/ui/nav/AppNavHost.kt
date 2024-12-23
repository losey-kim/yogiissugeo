package com.personal.yogiissugeo.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.personal.yogiissugeo.ui.list.BinListViewModel
import com.personal.yogiissugeo.ui.list.ClothingBinScreen
import com.personal.yogiissugeo.ui.map.NaverMapScreen

//네비게이션 루트 상수
object NavRoutes {
    const val BinList = "binListScreen"
    const val Map = "mapScreen"
}

/**
 * 앱 내 내비게이션을 관리하는 컴포저블 함수
 *
 * @param navController 네비게이션을 위한 NavHostController
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    // NavHost: 앱의 네비게이션 그래프를 정의합니다.
    NavHost(
        navController = navController, // 네비게이션 컨트롤러
        startDestination = NavRoutes.BinList // 앱 시작 시 보여줄 화면 (binListScreen)
    ) {
        // "binListScreen" 경로로 접근 시 의류 수거함 목록 화면을 표시
        composable(NavRoutes.BinList) {
            val viewModel: BinListViewModel = hiltViewModel()
            ClothingBinScreen(navController, viewModel) // ClothingBinScreen 컴포저블로 이동
        }

        composable(NavRoutes.Map) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.BinList)
            }
            val viewModel: BinListViewModel = hiltViewModel(parentEntry)
            NaverMapScreen(viewModel)
        }
    }
}