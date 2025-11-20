package com.example.my_uz_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.my_uz_android.navigation.AppNavigation
import com.example.my_uz_android.ui.theme.MyUZTheme
// Importujemy moduł, aby upewnić się, że Supabase jest dostępny (opcjonalne, ale dobre dla pewności)
import com.example.my_uz_android.di.AppModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja Supabase (opcjonalnie, jeśli chcesz wymusić start od razu,
        // choć 'by lazy' w AppModule zrobi to przy pierwszym użyciu)
        // val supabase = AppModule.supabase

        enableEdgeToEdge()
        setContent {
            MyUZTheme {
                // Tutaj po prostu ładujemy Twoją nawigację
                AppNavigation()
            }
        }
    }
}