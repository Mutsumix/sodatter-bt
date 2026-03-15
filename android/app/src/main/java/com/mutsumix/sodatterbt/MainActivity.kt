package com.mutsumix.sodatterbt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mutsumix.sodatterbt.navigation.AppNavHost
import com.mutsumix.sodatterbt.ui.theme.SodatterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SodatterTheme {
                AppNavHost()
            }
        }
    }
}
