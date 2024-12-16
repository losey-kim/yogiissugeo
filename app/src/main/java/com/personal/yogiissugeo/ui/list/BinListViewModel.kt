package com.personal.yogiissugeo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.yogiissugeo.R
import com.personal.yogiissugeo.data.model.ApiSource
import com.personal.yogiissugeo.data.model.ClothingBin
import com.personal.yogiissugeo.data.repository.ClothingBinRepository
import com.personal.yogiissugeo.utils.ResourceException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 의류 수거함 데이터를 관리하는 ViewModel 클래스.
 * API 호출, 상태 관리, 사용자 상호작용 처리 등을 담당합니다.
 *
 * @property clothingBinRepository 의류 수거함 데이터를 가져오는 Repository.
 */
@HiltViewModel
class BinListViewModel @Inject constructor(
    private val clothingBinRepository: ClothingBinRepository // 의류 수거함 데이터를 가져오는 레포지토리
) : ViewModel() {
    val itemsPerPage = 3 // 페이지당 항목 수

    /**
     * 로딩 상태를 나타냅니다.
     * true일 경우 데이터 로딩 중, false일 경우 로딩 완료 상태.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * 의류 수거함 데이터를 저장하는 상태.
     * 서버에서 가져온 데이터를 리스트 형태로 저장.
     */
    private val _clothingBins = MutableStateFlow<List<ClothingBin>>(emptyList())
    val clothingBins: StateFlow<List<ClothingBin>> = _clothingBins

    /**
     * 에러 메시지를 저장하는 상태.
     */
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage

    /**
     * 현재 페이지 번호를 나타내는 상태.
     * 페이지 전환 시 업데이트됨.
     */
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    /**
     * 선택한 구의 API 소스를 저장하는 상태.
     */
    private val _selectedApiSource = MutableStateFlow<ApiSource?>(null)
    val selectedApiSource: StateFlow<ApiSource?> = _selectedApiSource.asStateFlow()

    /**
     * 특정 구와 페이지의 의류 수거함 데이터를 서버에서 가져옵니다.
     *
     * @param district 요청할 구의 API 소스.
     * @param page 요청할 페이지 번호.
     * @param perPage 한 페이지당 데이터 개수.
     */
    private fun loadClothingBins(district: ApiSource, page: Int, perPage: Int) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            _errorMessage.value = null // 에러 메시지 초기화

            val binsResult = clothingBinRepository.getClothingBins(district, page, perPage)
            binsResult.onSuccess { bins ->
                bins.formattedData?.let {
                    _clothingBins.value = bins.formattedData // 데이터를 상태에 반영
                }
            }.onFailure {
                handleApiFailure(it) // 에러 처리
            }

            _isLoading.value = false // 로딩 종료
        }
    }

    /**
     * API 호출 실패 시 에러를 처리합니다.
     *
     * @param error API 호출 중 발생한 예외.
     * - ResourceException: 사용자에게 표시할 에러 메시지 ID를 설정.
     * - 기타 예외: 기본 에러 메시지로 처리.
     */
    private fun handleApiFailure(error: Throwable) {
        _errorMessage.value = when (error) {
            is ResourceException -> error.errorResId
            else -> R.string.error_unknown
        }
    }

    /**
     * 특정 구를 선택한 후 데이터를 초기 로드합니다.
     *
     * @param apiSource 선택된 구의 API 소스.
     * @param page 초기 페이지 번호.
     * @param perPage 한 페이지당 데이터 개수.
     */
    fun onDistrictSelected(apiSource: ApiSource, page: Int, perPage: Int) {
        _selectedApiSource.value = apiSource
        _currentPage.value = 1 // 페이지 초기화
        loadClothingBins(apiSource, page, perPage)
    }

    /**
     * 다음 페이지 데이터를 로드하는 함수
     *
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun goToNextPage(perPage: Int) {
        val apiSource = _selectedApiSource.value ?: return
        val nextPage = _currentPage.value + 1
        _currentPage.value = nextPage // 현재 페이지 번호 증가
        loadClothingBins(apiSource, _currentPage.value, perPage) // 다음 페이지 데이터 로드
    }

    /**
     * 이전 페이지 데이터를 로드하는 함수
     *
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun goToPreviousPage(perPage: Int) {
        val apiSource = _selectedApiSource.value ?: return
        val prevPage = (_currentPage.value - 1).coerceAtLeast(1) // 현재 페이지 번호 감소
        _currentPage.value = prevPage
        loadClothingBins(apiSource, _currentPage.value, perPage) // 이전 페이지 데이터 로드
    }
}