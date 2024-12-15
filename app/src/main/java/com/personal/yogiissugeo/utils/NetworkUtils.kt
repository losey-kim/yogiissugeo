package com.personal.yogiissugeo.utils

import com.personal.yogiissugeo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * 네트워크 API 호출을 안전하게 실행하며, 일반적인 예외를 처리하고 Result 객체를 반환합니다.
 *
 * @param apiCall 실행할 suspend API 호출 함수입니다.
 * @return Result<T> 성공적인 응답 본문 또는 적절한 에러를 포함합니다.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return withContext(Dispatchers.IO) { // 네트워크 작업을 IO 스레드에서 실행
        try {
            // API 호출 실행
            val response = apiCall()

            // API 응답이 성공적인지 확인
            if (response.isSuccessful) {
                response.body()?.let {
                    // 응답 본문이 null이 아닌 경우 성공으로 반환
                    Result.success(it)
                } ?: Result.failure(ResourceException(R.string.error_unknown)) // 응답 본문이 null일 경우
            } else {
                // API 호출이 실패한 경우 실패로 반환
                Result.failure(ResourceException(R.string.error_unknown))
            }
        } catch (e: TimeoutException) {
            // 요청 시간이 초과된 경우의 예외 처리
            Result.failure(ResourceException(R.string.error_timeout))
        } catch (e: IOException) {
            // 네트워크 연결 문제 또는 소켓 관련 오류 처리
            Result.failure(ResourceException(R.string.error_network))
        } catch (e: Exception) {
            // 기타 예상하지 못한 예외 처리
            Result.failure(ResourceException(R.string.error_unknown))
        }
    }
}