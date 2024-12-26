package com.yogiissugeo.android.ui

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.yogiissugeo.android.R
import com.yogiissugeo.android.ui.nav.AppNavHost
import com.yogiissugeo.android.ui.nav.BottomNavigationBar

@Composable
fun YigApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar() //상단 앱바
        },
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding() // 시스템 내비게이션 영역만큼 패딩 추가
            ) {
                // 하단 배너 광고
                AdMobBannerView(adUnitId = "ca-app-pub-4848324410383539/5955518384")

                // 하단 네비게이션 바
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 메인 화면 콘텐츠
            Column(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    )
            ) {
                AppNavHost(navController = navController)
            }
        }
    }
}

/**
 * 툴바를 표시하는 컴포저블
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar() {
    // 툴바
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        modifier = Modifier
    )
}

@Composable
fun AdMobBannerView(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // AdSize 계산 로직
    val adSize by remember {
        mutableStateOf(getAdSize(context))
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(adSize)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}

// Helper function: Get adaptive ad size based on screen width
private fun getAdSize(context: Context): AdSize {
    val displayMetrics = context.resources.displayMetrics
    val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = context.getSystemService(WindowManager::class.java).currentWindowMetrics
        windowMetrics.bounds.width()
    } else {
        displayMetrics.widthPixels
    }
    val density = displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}