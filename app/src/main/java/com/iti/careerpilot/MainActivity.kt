package com.iti.careerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.ui.theme.CareerPilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareerPilotTheme {
                RootNavDisplay()
            }
        }
    }
}
