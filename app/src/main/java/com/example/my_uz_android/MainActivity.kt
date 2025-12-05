package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.theme.MyUZTheme // <-- Poprawiona nazwa importu

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Używamy poprawnej nazwy motywu zdefiniowanej w Theme.kt
            MyUZTheme {
                // Musimy podać ekran startowy.
                // Ustawiam "landing" (Onboarding), ale możesz zmienić na "main", jeśli chcesz go pominąć.
                AppNavigation(startDestination = "landing")
            }
        }
    }
}