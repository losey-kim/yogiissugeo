package com.personal.yogiissugeo.utils.network

import androidx.annotation.StringRes

/**
 * 리소스 ID 기반의 예외를 나타내는 클래스.
 * @param errorResId 에러 메시지를 나타내는 리소스 ID. UI에서 메시지를 로드할 때 사용.
 * @param errorMessage 선택적 추가 에러 메시지.
 */
class ResourceException(
    @StringRes val errorResId: Int, // 에러 메시지 리소스 ID
    val errorMessage: String? = null // 선택적 추가 메시지
) : Exception()