package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AtsAppNavigation
import com.example.ui.theme.AiResumeAtsTheme
import com.example.ui.viewmodel.AtsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiResumeAtsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: AtsViewModel = viewModel()
                    AtsAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
