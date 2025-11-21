package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.screens.onboarding.LandingScreen
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUZTheme {
                // Prosta logika stanu: Czy onboarding został zakończony?
                // W prawdziwej aplikacji zapisałbyś to w SharedPreferences / DataStore
                var isOnboardingFinished by remember { mutableStateOf(false) }

                if (!isOnboardingFinished) {
                    // Wyświetlamy Onboarding
                    LandingScreen(
                        onFinishOnboarding = {
                            // Użytkownik kliknął "Pomiń" lub "Gotowe!"
                            isOnboardingFinished = true
                        }
                    )
                } else {
                    // Wyświetlamy Główną Aplikację
                    AppNavigation()
                }
            }
        }
    }
}