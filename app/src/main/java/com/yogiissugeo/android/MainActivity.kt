package com.yogiissugeo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yogiissugeo.android.ui.AppEntryPoint
import com.yogiissugeo.android.ui.theme.YogiIssugeoTheme
import com.yogiissugeo.android.utils.config.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //RemoteConfigManager
    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogiIssugeoTheme{
                AppEntryPoint(remoteConfigManager)
            }
        }
    }
}
