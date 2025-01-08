package com.yogiissugeo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yogiissugeo.android.ui.AppEntryPoint
import com.yogiissugeo.android.ui.theme.YogiIssugeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogiIssugeoTheme{
                AppEntryPoint()
            }
        }
    }
}
