package com.personal.yogiissugeo.ui.nav

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.personal.yogiissugeo.ui.list.ClothingBinScreen
import com.personal.yogiissugeo.ui.map.NaverMapScreen

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
        startDestination = "binListScreen" // 앱 시작 시 보여줄 화면 (binListScreen)
    ) {
        // "binListScreen" 경로로 접근 시 의류 수거함 목록 화면을 표시
        composable("binListScreen") {
            ClothingBinScreen(navController) // ClothingBinScreen 컴포저블로 이동
        }

        // "mapScreen/{latitude}/{longitude}" 경로로 접근 시 지도 화면을 표시
        composable(
            "mapScreen/{latitude}/{longitude}", // 경로에 latitude와 longitude를 포함
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType }, // latitude 파라미터는 String으로 받음
                navArgument("longitude") { type = NavType.StringType } // longitude 파라미터는 String으로 받음
            )
        ) { backStackEntry ->
            // 경로에서 latitude와 longitude 파라미터 값을 가져옵니다.
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()

            // latitude와 longitude 값이 유효하면 MapScreen을 표시, 그렇지 않으면 에러 메시지를 표시
            if (latitude != null && longitude != null) {
                NaverMapScreen(latitude, longitude)
            } else {
                Text("잘못된 좌표 정보입니다.") // 잘못된 좌표일 경우 에러 메시지 표시
            }
        }
    }
}