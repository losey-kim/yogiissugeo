package com.personal.yogiissugeo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.personal.yogiissugeo.ui.nav.AppNavHost
import com.personal.yogiissugeo.ui.theme.YogiIssugeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogiIssugeoTheme{
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
