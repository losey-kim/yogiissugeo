package com.yogiissugeo.android.utils.navigation

import androidx.navigation.NavController

/**
 * 네비게이션 확장 함수
 * 화면 중복 생성 방지, 이전 상태 복원
 */
fun NavController.navigateWithOptions(route: String) {
    this.navigate(route) {
        // 시작 지점으로 돌아가는 백스택 설정
        popUpTo(this@navigateWithOptions.graph.startDestinationId) { saveState = true }
        launchSingleTop = true // 중복된 화면 생성 방지
        restoreState = true // 이전 상태 복원
    }
}