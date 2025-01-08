package com.yogiissugeo.android.utils.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object VersionUtils {
    /**
     * 현재 버전 코드
     */
    fun getCurrentAppVersion(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            1L // 기본값 설정
        }
    }
}