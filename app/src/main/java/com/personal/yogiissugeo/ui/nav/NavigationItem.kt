package com.personal.yogiissugeo.ui.nav

import com.personal.yogiissugeo.R

// 네비게이션 항목을 정의하는 sealed class
sealed class NavigationItem(
    val route: String, //경로
    val title: String, //제목
    val icon: Int, //기본 아이콘
    val selectedIcon: Int //선택 아이콘
) {
    data object Map : NavigationItem("map", "지도", R.drawable.ic_map, R.drawable.ic_map_border)
    data object Saved :
        NavigationItem("saved", "저장됨", R.drawable.ic_bookmark, R.drawable.ic_bookmark_border)
}