package com.personal.yogiissugeo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.yogiissugeo.R
import com.personal.yogiissugeo.data.model.ClothingBin
import com.personal.yogiissugeo.data.repository.ClothingBinRepository
import com.personal.yogiissugeo.utils.ResourceException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinListViewModel @Inject constructor(
    private val clothingBinRepository: ClothingBinRepository // 의류 수거함 데이터를 가져오는 레포지토리
) : ViewModel() {
    // 로딩 상태를 나타내는 MutableStateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading // 외부에서 접근할 수 있도록 StateFlow로 제공

    // 의류 수거함 데이터를 저장하는 MutableStateFlow
    private val _clothingBins = MutableStateFlow<List<ClothingBin>>(emptyList())
    val clothingBins: StateFlow<List<ClothingBin>> = _clothingBins // 외부에서 접근할 수 있도록 StateFlow로 제공

    // 에러 메시지 상태를 저장하는 MutableStateFlow
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage // 리소스 ID를 통해 에러 메시지를 처리

    // 현재 페이지 상태를 나타내는 MutableStateFlow
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage // 외부에서 접근할 수 있도록 StateFlow로 제공

    /**
     * 의류 수거함 데이터를 로드하는 함수
     *
     * @param page 요청할 페이지 번호
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun loadClothingBins2(page: Int, perPage: Int) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 상태를 true로 설정
            _errorMessage.value = null // 이전 에러 메시지를 초기화

            // API 요청
            val binsResult = clothingBinRepository.getClothingBins(page, perPage)
            binsResult.onSuccess { response ->
                val bins = response.data ?: emptyList()

                // Bin 데이터에 좌표를 추가
                val enrichedBins = bins.mapNotNull { bin ->
                    val correctedAddress = correctAddress(bin.address) // 주소 정제
                    val geoResult = clothingBinRepository.getCoordinates(correctedAddress) // 좌표 요청

                    // 주소 좌표 변환 결과 처리
                    geoResult.fold(
                        onSuccess = { geoData ->
                            val coordinates = geoData.addresses.firstOrNull() // 첫 번째 주소 좌표 가져오기
                            bin.copy(
                                address = correctedAddress, // 정제된 주소로 업데이트
                                latitude = coordinates?.y,  // 위도
                                longitude = coordinates?.x  // 경도
                            )
                        },
                        onFailure = {
                            // 좌표 변환 실패 시 에러 메시지를 리소스 ID로 설정
                            if (it is ResourceException) {
                                _errorMessage.value = it.resourceId
                            } else {
                                _errorMessage.value = R.string.error_unknown // 알 수 없는 에러 처리
                            }
                            null // 실패한 Bin은 제외
                        }
                    )
                }

                _clothingBins.value = enrichedBins // 좌표가 추가된 의류 수거함 리스트를 상태에 반영
            }.onFailure {
                // API 호출 실패 시 에러 처리
                if (it is ResourceException) {
                    _errorMessage.value = it.resourceId // 에러 메시지를 리소스 ID로 설정
                } else {
                    _errorMessage.value = R.string.error_unknown // 알 수 없는 에러 처리
                }
            }

            _isLoading.value = false // 로딩 상태를 false로 설정
        }
    }

    /**
     * 의류 수거함 데이터를 로드하는 함수
     */
    fun loadClothingBins(page: Int, perPage: Int) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 상태를 true로 설정
            _errorMessage.value = null // 에러 메시지 초기화

            // API 요청
            val binsResult = clothingBinRepository.getClothingBins(page, perPage)
            binsResult.onSuccess { response ->
                val enrichedBins = processBinData(response.data ?: emptyList())
                _clothingBins.value = enrichedBins // 좌표가 추가된 의류 수거함 리스트를 상태에 반영
            }.onFailure {
                handleApiFailure(it) // 실패 처리
            }

            _isLoading.value = false // 로딩 상태를 false로 설정
        }
    }

    /**
     * 좌표 변환 및 에러 처리 함수
     */
    private suspend fun processBinData(bins: List<ClothingBin>): List<ClothingBin> {
        // Bin 데이터에 좌표를 추가
        return bins.mapNotNull { bin ->
            val correctedAddress = correctAddress(bin.address) // 주소 정제
            val geoResult = clothingBinRepository.getCoordinates(correctedAddress) // 좌표 요청

            // 주소 좌표 변환 결과 처리
            geoResult.fold(
                onSuccess = { geoData ->
                    val coordinates = geoData.addresses.firstOrNull() // 첫 번째 주소 좌표 가져오기
                    bin.copy(
                        address = correctedAddress, // 정제된 주소로 업데이트
                        latitude = coordinates?.y, // 위도
                        longitude = coordinates?.x //경도
                    )
                },
                onFailure = {
                    handleApiFailure(it) // 좌표 변환 실패 처리
                    null // 실패한 Bin 제외
                }
            )
        }
    }

    /**
     * API 호출 실패 시 에러 처리
     */
    private fun handleApiFailure(error: Throwable) {
        _errorMessage.value = when (error) {
            is ResourceException -> error.resourceId // 에러 메시지를 리소스 ID로 설정
            else -> R.string.error_unknown // 기본 에러 처리
        }
    }

    /**
     * 다음 페이지 데이터를 로드하는 함수
     *
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun goToNextPage(perPage: Int) {
        _currentPage.value += 1 // 현재 페이지 번호 증가
        loadClothingBins(_currentPage.value, perPage) // 다음 페이지 데이터 로드
    }

    /**
     * 이전 페이지 데이터를 로드하는 함수
     *
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun goToPreviousPage(perPage: Int) {
        if (_currentPage.value > 1) {
            _currentPage.value -= 1 // 현재 페이지 번호 감소
            loadClothingBins(_currentPage.value, perPage) // 이전 페이지 데이터 로드
        }
    }

    /**
     * 주소를 정제하는 함수 (예: "서울특별기"를 "서울특별시"로 수정)
     *
     * @param address 주소 문자열
     * @return 정제된 주소
     */
    private fun correctAddress(address: String): String {
        return address.replace("서울특별기", "서울특별시") // 오타 수정
    }
}