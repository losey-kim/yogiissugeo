package com.yogiissugeo.android.utils.common

import android.content.Context
import java.io.InputStream

/**
 * Assets 폴더에서 지정된 CSV 파일을 로드하여 InputStream으로 반환하는 함수.
 * 파일을 성공적으로 읽어오면 `onCsvLoaded` 콜백을 호출하고, 실패하면 `onError` 콜백을 호출합니다.
 *
 * @param fileName 로드할 CSV 파일의 이름.
 * @param onCsvLoaded 파일 로드 성공 시 호출.
 * @param onError 파일 로드 실패 시 호출.
 */
fun Context.loadCsvFromAssets(
    fileName: String,
    onCsvLoaded: (InputStream) -> Unit,
    onError: (Exception) -> Unit
) {
    try {
        // Assets 폴더에서 지정된 파일을 열고 InputStream을 반환
        val inputStream = assets.open(fileName)

        // 파일 로드 성공 시 콜백 호출
        onCsvLoaded(inputStream)

        // InputStream 닫기
        inputStream.close()
    } catch (e: Exception) {
        // 파일 로드 실패 시 에러 콜백 호출
        onError.invoke(e)
    }
}