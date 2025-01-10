package com.yogiissugeo.android.ui.setting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.app.LegalNoticeActivity
import com.naver.maps.map.app.OpenSourceLicenseActivity
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.RecyclingInfo

@Composable
fun SettingScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    //서울시 구별 재활용 정보(담당부서 및 홈페이지)
    val recyclingInfo by viewModel.recyclingInfo.collectAsState()
    //재활용 정보 다이얼로그 출력 여부
    val showRecyclingDialog by viewModel.showRecyclingBottomSheet.collectAsState()

    //설정 아이템 추가
    val categorizedSettings = listOf(
        // 헤더 - 기본 설정
        SettingListItem.Category(stringResource(R.string.setting_menu_header_common)),
        // 재활용 정보
        SettingListItem.Item(
            SettingItem(stringResource(R.string.setting_menu_recycling_info)) {
                viewModel.toggleRecyclingBottomSheet(true)
            }
        ),
        // 위치 권한 설정
        SettingListItem.Item(
            SettingItem(stringResource(R.string.setting_menu_location_permissions)) {
                openSettings(context)
            }
        ),
        // 헤더 - 도움말
        SettingListItem.Category(stringResource(R.string.setting_menu_header_helper)),
        // 오류 제보
        SettingListItem.Item(
            SettingItem(stringResource(R.string.setting_menu_reporting_error)) {
                sendErrorReport(context)
            }
        ),
        // 헤더 - 법적 고지
        SettingListItem.Category(stringResource(R.string.setting_menu_header_legal)),
        // 네이버 법적 고지
        SettingListItem.Item(
            SettingItem(stringResource(R.string.setting_menu_naver_legal_notice)) {
                val intent = Intent(context, LegalNoticeActivity::class.java)
                context.startActivity(intent)
            }
        ),
        // 네이버 지도 오픈소스 라이선스
        SettingListItem.Item(
            SettingItem(stringResource(R.string.setting_menu_naver_open_source_license)) {
                val intent = Intent(context, OpenSourceLicenseActivity::class.java)
                context.startActivity(intent)
            }
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        //설정 아이템 출력
        itemsIndexed(categorizedSettings, key = { index, item ->
            when (item) {
                is SettingListItem.Category -> "category_${item.title}"
                is SettingListItem.Item -> "item_${item.setting.title}"
            }
        }) { index, item ->
            when (item) {
                // 메뉴 헤더
                is SettingListItem.Category -> {
                    if (index != 0) { // 첫 번째 메뉴가 아닌 경우 디바이더 출력
                        HorizontalDivider(Modifier.height(8.dp))
                    }
                    CategoryHeader(item.title)
                }

                // 메뉴 아이템
                is SettingListItem.Item -> {
                    SettingRow(item.setting)
                }
            }
        }
    }

    //재활용 정보 다이얼로그 출력 
    if (showRecyclingDialog) {
        RecyclingInfoBottomSheet(
            context,
            recyclingInfo = recyclingInfo,
            onDismiss = {
                viewModel.toggleRecyclingBottomSheet(false)
            }
        )
    }
}

// 메뉴 헤더
@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)

    )
}

// 개별 설정 아이템
@Composable
fun SettingRow(
    setting: SettingItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                setting.onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

//지역별 재활용 담당부서 나타내는 바텀 모달시트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingInfoBottomSheet(
    context: Context,
    recyclingInfo: List<RecyclingInfo>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    // 바텀 모달시트
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 타이틀
            Text(
                text = stringResource(R.string.setting_menu_recycling_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(recyclingInfo) { info ->
                    // 각 구별 정보 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_card)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // 지역
                            Text(
                                text = info.district,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 담당 부서
                            Text(
                                text = info.department,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // 링크
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.url))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_link),
                                    contentDescription = "link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Uri.parse(info.url).host ?: info.url,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 오류제보 이메일
 */
fun sendErrorReport(context: Context) {
    val appVersion = getAppVersion(context)
    val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}" // 기기 제조사 + 모델명
    val systemVersion = Build.VERSION.RELEASE // 시스템 버전
    val apiLevel = Build.VERSION.SDK_INT // API 수준

    // 이메일 제목
    val emailSubject = context.getString(R.string.error_report_subject)
    // 이메일 본문
    val emailBody = context.getString(
        R.string.error_report_body,
        appVersion,
        deviceInfo,
        systemVersion,
        apiLevel
    )

    // 메일 전송 Intent 생성
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.error_report_email)))
        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    try {
        context.startActivity(
            Intent.createChooser(
                emailIntent,
                context.getString(R.string.error_report_chooser_title)
            )
        )
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_report_sent, Toast.LENGTH_SHORT).show()
        Log.e("SettingScreen", "오류 보고서를 보내는 데 실패했습니다.", e)
    }
}

// 앱 버전 가져오기
fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: context.getString(R.string.unknown_version)
    } catch (e: PackageManager.NameNotFoundException) {
        context.getString(R.string.unknown_version)
    }
}

// 설정 이동 함수
fun openSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_opening_settings, Toast.LENGTH_SHORT).show()
        Log.e("SettingScreen", "설정을 여는 데 실패했습니다.", e)
    }
}

sealed class SettingListItem {
    data class Category(val title: String) : SettingListItem()
    data class Item(val setting: SettingItem) : SettingListItem()
}

// 설정 아이템 데이터를 정의하는 클래스
data class SettingItem(
    val title: String, //제목
    val onClick: () -> Unit //클릭
)