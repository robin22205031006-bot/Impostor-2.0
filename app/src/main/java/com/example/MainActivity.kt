package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WordBank
import com.example.ui.screens.LocalGameScreen
import com.example.ui.theme.ImpostorTheme
import com.example.viewmodel.LocalGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WordBank.loadFromAssets(applicationContext)
        enableEdgeToEdge()
        setContent {
            ImpostorTheme {
                val localViewModel: LocalGameViewModel = viewModel(key = "local_game")
                LocalGameScreen(viewModel = localViewModel)
            }
        }
    }
}
