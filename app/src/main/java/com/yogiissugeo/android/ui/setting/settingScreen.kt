package com.yogiissugeo.android.ui.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.map.app.LegalNoticeActivity
import com.naver.maps.map.app.OpenSourceLicenseActivity
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.RecyclingInfo

//TODO 설정화면 개발 필요
@Composable
fun SettingScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    //재활용 정보 다이얼로그 출력 여부
    var showRecyclingDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    //서울시 구별 재활용 정보(담당부서 및 홈페이지)
    val recyclingInfo by viewModel.recyclingInfo.collectAsState()

    //설정 아이템 추가
    val settingItem = listOf(
        SettingItem(stringResource(R.string.setting_menu_recycling_info), {
            showRecyclingDialog = true
        }),
        SettingItem(stringResource(R.string.setting_menu_naver_legal_notice), {
            val intent = Intent(context, LegalNoticeActivity::class.java)
            context.startActivity(intent)
        }),
        SettingItem(stringResource(R.string.setting_menu_naver_open_source_license), {
            val intent = Intent(context, OpenSourceLicenseActivity::class.java)
            context.startActivity(intent)
        })
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        //설정 아이템 출력
        itemsIndexed(settingItem) { index, setting ->
            // 각 설정 항목을 SettingRow 함수로 출력
            SettingRow(setting)
            if (index < settingItem.lastIndex) {
                HorizontalDivider()
            }
        }
    }


    //재활용 정보 다이얼로그 출력 
    if (showRecyclingDialog) {
        RecyclingInfoDialog(
            context,
            recyclingInfo = recyclingInfo,
            onDismiss = { showRecyclingDialog = false }
        )
    }
}

// 개별 설정 항목의 UI를 구성하는 함수
@Composable
fun SettingRow(setting: SettingItem) {
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
            Text(text = setting.title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

//지역별 재활용 정보를 나타내는 다이얼로그
//TODO UI 개선 필요
@Composable
fun RecyclingInfoDialog(context: Context, recyclingInfo: List<RecyclingInfo>, onDismiss: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.setting_menu_recycling_info)) },
        text = {
            LazyColumn {
                items(recyclingInfo) { info ->
                    Column {
                        Text(text = "${info.district}: ${info.department}")
                        Text(
                            text = info.url,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.url))
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.setting_close))
            }
        }
    )
}

// 설정 아이템 데이터를 정의하는 클래스
data class SettingItem(
    val title: String, //제목
    val onClick: () -> Unit //클릭
)