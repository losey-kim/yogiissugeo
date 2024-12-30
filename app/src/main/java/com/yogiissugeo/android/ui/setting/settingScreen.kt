package com.yogiissugeo.android.ui.setting

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.naver.maps.map.app.LegalNoticeActivity
import com.naver.maps.map.app.OpenSourceLicenseActivity

//TODO 설정화면 개발 필요
@Composable
fun SettingScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //설정 아이템 추가
        val settingItem = listOf(
            SettingItem("서울시 구별 재활용 정보", {
                RecyclingInfoDialog{

                }
            }),
            SettingItem("Naver Map Android SDK 법적 공지 / 정보 제공처", {
                val intent = Intent(context, LegalNoticeActivity::class.java)
                context.startActivity(intent)
            }),
            SettingItem("Naver Map Android SDK 오픈소스 라이선스", {
                val intent = Intent(context, OpenSourceLicenseActivity::class.java)
                context.startActivity(intent)
            })
        )
        //설정 아이템 출력
        itemsIndexed(settingItem) { index, setting ->
            // 각 설정 항목을 SettingRow 함수로 출력
            SettingRow(setting)
            if (index < settingItem.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

// 개별 설정 항목의 UI를 구성하는 함수
@Composable
fun SettingRow(setting: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
//                setting.onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = setting.title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RecyclingInfoDialog(onDismiss: () -> Unit) {
    val recyclingInfo = listOf(
        Gucheong(
            "강남구",
            "자원순환과",
            "02-3423-5976",
            "https://www.gangnam.go.kr/board/waste/list.do?mid=ID02_011109"
        ),
        Gucheong(
            "강동구",
            "청소행정과 청소행정팀",
            "02-3425-5860",
            "https://www.gangdong.go.kr/web/newportal/bbs/b_135/list"
        ),
        Gucheong(
            "강북구",
            "청소행정과",
            "02-901-6766",
            "https://www.gangbuk.go.kr:18000/portal/bbs/B0000089/list.do?menuNo=200294&nttId=85379"
        )
    )

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        title = { Text("서울시 구별 재활용 정보") },
        text = {
            LazyColumn {
                items(recyclingInfo) { info ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = "구: ${info.district}", fontWeight = FontWeight.Bold)
                        Text(text = "담당 부서: ${info.part}")
                        Text(text = "전화번호: ${info.number}")
                        ClickableText(
                            text = AnnotatedString(info.url),
                            onClick = { /* 링크를 처리하는 로직 추가 */ }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("닫기")
            }
        }
    )
}

// 설정 아이템 데이터를 정의하는 클래스
data class SettingItem(
    val title: String, //제목
    val onClick: @Composable () -> Unit //클릭
)

// 설정 아이템 데이터를 정의하는 클래스
data class Gucheong(
    val district: String, //제목
    val part: String,
    val number: String,
    val url: String
)

