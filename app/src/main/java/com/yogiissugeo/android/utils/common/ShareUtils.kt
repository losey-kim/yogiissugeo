package com.yogiissugeo.android.utils.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.yogiissugeo.android.BuildConfig
import com.yogiissugeo.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 네이버 지도 앱을 열어 특정 위치에 마커를 표시.
     * @param latLng 위치 정보 (위도와 경도)
     * @param address 마커에 표시될 주소
     */
    fun navigateToNaverMap(latLng: LatLng, address: String) {
        val uri = Uri.Builder()
            .scheme("nmap") // 네이버 지도 스키마
            .authority("place")
            .appendQueryParameter("lat", latLng.latitude.toString()) // 위도
            .appendQueryParameter("lng", latLng.longitude.toString()) // 경도
            .appendQueryParameter("name", address) // 주소
            .appendQueryParameter("appname", BuildConfig.APPLICATION_ID) // 앱 이름
            .build()

        // Intent 생성
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        try {
            // 네이버 지도 앱이 설치되어 있는 경우 앱 열기
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException){
            // 네이버 지도 앱이 설치되어 있지 않은 경우 Play 스토어로 이동
            val marketIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.nhn.android.nmap")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        }
    }

    /**
     * 주소 공유
     * @param address 공유할 주소
     */
    fun shareAddress(address: String) {
        // 공유 인텐트 생성
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 텍스트 타입 지정
            putExtra(Intent.EXTRA_TEXT, address) // 공유할 텍스트 추가
        }

        // 공유 선택기(Chooser) 인텐트 생성
        val chooser = Intent.createChooser(sendIntent, context.getString(R.string.bookmarks_share)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser) // 공유 선택기 인텐트 실행
        } catch (e: Exception) {
            // 예외 발생 시 토스트 출력
            Toast.makeText(context, context.getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
        }
    }
}