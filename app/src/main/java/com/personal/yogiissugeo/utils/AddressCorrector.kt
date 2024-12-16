package com.personal.yogiissugeo.utils

/**
 * 주소 정제를 위한 유틸리티 클래스.
 * 주소의 오타나 불필요한 텍스트를 수정하여 표준화된 형식으로 반환합니다.
 */
object AddressCorrector {
    private val corrections = mapOf(
        "서울특별기" to "서울특별시"
    )

    /**
     * 주어진 주소 문자열을 정제합니다.
     *
     * @param address 원본 주소 문자열.
     * @return 정제된 주소 문자열.
     */
    fun correct(address: String): String {
        var correctedAddress = address
        corrections.forEach { (incorrect, correct) ->
            correctedAddress = correctedAddress.replace(incorrect, correct)
        }
        return correctedAddress
    }
}