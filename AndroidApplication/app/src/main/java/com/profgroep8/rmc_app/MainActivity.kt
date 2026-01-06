package com.profgroep8.rmc_app

import RmcApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.profgroep8.rmc_app.ui.theme.RMCappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RMCappTheme {
                RmcApp()
            }
        }
    }
}