package com.yogiissugeo.android.ui.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.yogiissugeo.android.R

// 네비게이션 항목을 정의하는 sealed class
sealed class NavigationItem(
    val route: String, //경로
    @StringRes val title: Int, //제목
    @DrawableRes val icon: Int, //기본 아이콘
    @DrawableRes val selectedIcon: Int //선택 아이콘
) {
    data object Map : NavigationItem("map", R.string.nav_map, R.drawable.ic_map_search, R.drawable.ic_map_search_fill)
    data object Saved : NavigationItem("saved", R.string.nav_saved, R.drawable.ic_heart, R.drawable.ic_heart_fill)
    data object Setting: NavigationItem("setting", R.string.nav_setting, R.drawable.ic_setting, R.drawable.ic_setting_fill)
}