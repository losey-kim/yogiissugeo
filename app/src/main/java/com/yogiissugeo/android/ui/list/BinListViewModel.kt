package com.yogiissugeo.android.ui.list

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.BookmarkType
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.data.repository.ClothingBinRepository
import com.yogiissugeo.android.utils.network.ResourceException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/**
 * 의류 수거함 데이터를 관리하는 ViewModel 클래스.
 * API 호출, 상태 관리, 사용자 상호작용 처리 등을 담당합니다.
 *
 * @property clothingBinRepository 의류 수거함 데이터를 가져오는 Repository.
 */
@HiltViewModel
class BinListViewModel @Inject constructor(
    private val clothingBinRepository: ClothingBinRepository // 의류 수거함 데이터를 가져오는 레파지토리
) : ViewModel() {

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
     * 선택한 구의 API 소스를 저장하는 상태.
     */
    private val _selectedApiSource = MutableStateFlow<ApiSource?>(null)
    val selectedApiSource: StateFlow<ApiSource?> = _selectedApiSource.asStateFlow()

    /**
     * 즐겨찾기된 수거함 데이터를 페이징 형태로 가져옵니다.
     * 저장소에서 가져온 데이터를 Flow로 제공.
     */
    // 선택된 구에 따라 필터링된 북마크된 목록을 제공
    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarksBins: Flow<PagingData<ClothingBin>> = _selectedApiSource
        .flatMapLatest { district ->
            clothingBinRepository.getBookmarkBinsPaged(district?.name)
        }
        .cachedIn(viewModelScope)

    // 선택된 구에 따라 필터링된 총 개수를 제공
    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarkCount: StateFlow<Int> = _selectedApiSource
        .flatMapLatest { district ->
            clothingBinRepository.getBookmarkBinsCount(district?.name)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    /**
     * 저장된 수거함 데이터 전체를 가져옴
     */
    val allBookmarkedBins: Flow<List<ClothingBin>> = clothingBinRepository.getAllBookmarkedBins()

    /**
     * 북마크 토글 결과 전달
     */
    private val _bookmarkToggleResult = MutableSharedFlow<ShowSnackbar>()
    val bookmarkToggleResult = _bookmarkToggleResult.asSharedFlow()

    /**
     * 에러 메시지를 저장하는 상태.
     */
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage

    /**
     * 현재 페이지 번호를 나타내는 상태.
     * 페이지 전환 시 업데이트됨.
     */
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    /**
     * 전체 페이지 수를 나타내는 상태.
     */
    private val _totalPage = MutableStateFlow(0)
    val totalPage: StateFlow<Int> = _totalPage

    /**
     * 특정 구와 페이지의 의류 수거함 데이터를 가져옵니다.
     *
     * @param apiSource 요청할 구의 API 소스.
     * @param page 요청할 페이지 번호.
     * @param perPage 한 페이지당 데이터 개수.
     */
    private fun loadDistrictBins(apiSource: ApiSource, page: Int, perPage: Int) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            _errorMessage.value = null // 에러 메시지 초기화

            try {
                //데이터를 가져옴
                val result = clothingBinRepository.getOrFetchBins(apiSource, page, perPage)
                result.onSuccess { bins ->
                    _clothingBins.value = bins
                    // 현재 페이지 번호 증가
                    _currentPage.value += 1
                    // 구가 바뀌었으면 선택 구 갱신
                    if (_selectedApiSource.value != apiSource) {
                        _selectedApiSource.value = apiSource
                        //전체 페이지 수 계산(캐싱된 값이 있는지 확인)
                        _totalPage.value = clothingBinRepository.getTotalPage(apiSource, perPage)
                    }
                }.onFailure {
                    handleApiFailure(it) // 에러 처리
                }
            } catch (e: Exception) {
                handleApiFailure(e)
            } finally {
                _isLoading.value = false // 로딩 종료
            }
        }
    }

    //구 선택정보 저장
    fun setSelectedApiSource(apiSource: ApiSource?) {
        _selectedApiSource.value = apiSource
    }

    /**
     * 저장 상태를 toggle합니다.
     *
     * @param binId toggle할 binId
     */
    fun toggleBookmark(binId: String) = viewModelScope.launch {
        try {
            // 북마크 추가, 삭제
            val bookmarkType = clothingBinRepository.toggleBookmark(binId)
            // 북마크 추가, 삭제 여부에 따라 스낵바 출력 텍스트 리소스 아이디 저장
            val messageResId = when (bookmarkType){
                BookmarkType.ADD_SUCCESS -> R.string.bookmarks_add_success
                BookmarkType.REMOVE_SUCCESS -> R.string.bookmarks_remove_success
                BookmarkType.ERROR -> R.string.bookmarks_toggle_fail
            }
            _bookmarkToggleResult.emit(ShowSnackbar(messageResId, bookmarkType))
        } catch (e: Exception) {
            // 북마크 토글 실패
            _bookmarkToggleResult.emit(ShowSnackbar(R.string.bookmarks_toggle_fail, BookmarkType.ERROR))
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
            is HttpException -> R.string.error_server
            else -> R.string.error_unknown
        }
    }

    /**
     * 특정 구를 선택한 후 데이터를 초기 로드합니다.
     *
     * @param apiSource 선택된 구의 API 소스.
     * @param perPage 한 페이지당 데이터 개수.
     */
    fun onDistrictSelected(apiSource: ApiSource, perPage: Int = 100) {
        _currentPage.value = 0 // 페이지 초기화
        val nextPage = _currentPage.value + 1
        loadDistrictBins(apiSource, nextPage, perPage)
    }

    /**
     * 다음 페이지 데이터를 로드하는 함수
     *
     * @param perPage 한 페이지에 표시할 항목 수
     */
    fun goToNextPage(perPage: Int = 100) {
        val apiSource = _selectedApiSource.value ?: return
        val nextPage = _currentPage.value + 1
        loadDistrictBins(apiSource, nextPage, perPage) // 다음 페이지 데이터 로드
    }

    // 북마크 토글 이벤트
    data class ShowSnackbar(@StringRes val messageResId: Int, val bookmarkType: BookmarkType)
}