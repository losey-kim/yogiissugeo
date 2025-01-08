package com.yogiissugeo.android.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

/**
 * 기본 스낵바 출력
 */
suspend fun showSnackBar(snackbarHostState: SnackbarHostState, message: String, duration: SnackbarDuration){
    snackbarHostState.showSnackbar(
        message = message,
        duration = duration
    )
}