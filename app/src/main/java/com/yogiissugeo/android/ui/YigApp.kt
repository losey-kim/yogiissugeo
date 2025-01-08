package com.yogiissugeo.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.yogiissugeo.android.BuildConfig
import com.yogiissugeo.android.R
import com.yogiissugeo.android.ui.nav.AppNavHost
import com.yogiissugeo.android.ui.nav.BottomNavigationBar
import com.yogiissugeo.android.ui.splash.SplashScreen
import com.yogiissugeo.android.ui.splash.SplashViewModel


@Composable
fun AppEntryPoint() {
    // 초기화 상태 관리
    val splashViewModel: SplashViewModel = hiltViewModel()
    val isInitialized by splashViewModel.isInitialized.collectAsState()
    val forceUpdate by splashViewModel.forceUpdate.collectAsState()

    // 초기화 상태에 따라 다른 화면 표시
    if (isInitialized) {
        if (forceUpdate){ // 강제업데이트 필요 시 다이얼로그 출력
            ForceUpdateDialog()
        } else {
            val navController = rememberNavController()
            MainApp(navController)  // 초기화 완료 후 메인 화면
        }
    } else {
        SplashScreen() // 초기화 중 로딩 화면
    }
}

@Composable
fun MainApp(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.safeDrawingPadding(),
        containerColor = Color.Transparent,
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding() // 시스템 내비게이션 영역만큼 패딩 추가
            ) {
                // 하단 배너 광고
                AdMobBannerView(adUnitId = BuildConfig.ADMOB_BANNER_AD)

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
                text = stringResource(R.string.app_name)
            )
        },
        modifier = Modifier
    )
}

/**
 * 애드몹 배너 광고
 */
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

/**
 * 강제업데이트 다이얼로그
 */
@Composable
fun ForceUpdateDialog() {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {
        },
        title = { Text(stringResource(R.string.force_update_title)) },
        text = { Text(stringResource(R.string.force_update_message)) },
        confirmButton = {
            TextButton(onClick = {
                try {
                    // 업데이트 URL로 이동
                    val url = context.getString(R.string.playstore_link)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) {
                // 확인 버튼
                Text(stringResource(R.string.force_update_button))
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    )
}

@Preview
@Composable
fun TopAppBarPreview() {
    // 툴바
    TopAppBar()
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