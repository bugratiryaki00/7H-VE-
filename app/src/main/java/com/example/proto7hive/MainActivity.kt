package com.example.proto7hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.proto7hive.data.ThemePreferences
import com.example.proto7hive.ui.theme.Proto7HiveTheme
import com.example.proto7hive.ui.screens.RootScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val themePreferences = ThemePreferences(this)
        val initialDarkTheme = themePreferences.isDarkTheme()
        
        setContent {
            var darkTheme by remember { mutableStateOf(initialDarkTheme) }
            
            Proto7HiveTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScaffold(
                        onThemeChange = { isDark ->
                            darkTheme = isDark
                            themePreferences.setDarkTheme(isDark)
                        },
                        isDarkTheme = darkTheme
                    )
                }
            }
        }
    }
}