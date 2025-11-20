package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUZTheme {
                // Uruchamiamy główną nawigację aplikacji
                AppNavigation()
            }
        }
    }
}