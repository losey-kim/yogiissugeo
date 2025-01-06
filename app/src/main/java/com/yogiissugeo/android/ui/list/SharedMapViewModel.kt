package com.yogiissugeo.android.ui.list

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 선택한 좌표정보 공유 ViewModel 클래스
 */
@HiltViewModel
class SharedMapViewModel @Inject constructor() : ViewModel() {
    //선택좌표 값
    private val _selectedCoordinates = MutableStateFlow<LatLng?>(null)
    val selectedCoordinates: StateFlow<LatLng?> = _selectedCoordinates.asStateFlow()

    // 선택좌표 저장
    fun selectCoordinates(latLng: LatLng) {
        _selectedCoordinates.value = latLng
    }

    // 선택 좌표 초기화
    fun clearSelectedCoordinates() {
        _selectedCoordinates.value = null
    }
}