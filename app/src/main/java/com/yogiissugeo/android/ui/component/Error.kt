package com.yogiissugeo.android.ui.component

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * 에러 메시지를 표시하는 컴포저블
 *
 * @param context Context
 * @param resourceId 에러 메시지를 표시할 리소스 ID
 */
@Composable
fun ErrorMessage(context: Context, resourceId: Int) {
    Toast.makeText(context, stringResource(id = resourceId), Toast.LENGTH_SHORT).show()
}
