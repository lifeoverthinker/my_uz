package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.screens.account.AccountViewModel
import com.example.my_uz_android.ui.theme.MyUZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val accountViewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val settings by accountViewModel.settings.collectAsState()
            val darkTheme = settings?.isDarkMode ?: isSystemInDarkTheme()

            MyUZTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ POPRAWKA: Czekamy na załadowanie settings
                    if (settings == null) {
                        // Loading screen - pokazuje się tylko na start
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Dopiero po załadowaniu settings decydujemy o nawigacji
                        val startDestination = if (settings!!.selectedGroupCode.isNullOrEmpty()) {
                            "landing"
                        } else {
                            "main"
                        }

                        AppNavigation(startDestination = startDestination)
                    }
                }
            }
        }
    }
}
