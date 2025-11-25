package com.example.proto7hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.proto7hive.ui.theme.Proto7HiveTheme
import com.example.proto7hive.ui.screens.RootScaffold
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proto7HiveTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    RootScaffold()
                }
            }
        }
    }
}