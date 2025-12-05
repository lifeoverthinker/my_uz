package com.example.twojaaplikacja // <-- Upewnij się, że to Twoja paczka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- 1. ViewModel (Logika biznesowa) ---
// Przechowuje stan listy ocen, żeby nie znikały przy przechodzeniu między ekranami
class GradesViewModel : ViewModel() {
    // Przykładowe dane na start
    private val _grades = MutableStateFlow(
        listOf(
            Grade(value = "4", type = "Wejściówka", date = "20.09.2025"),
            Grade(value = "4.5", type = "Kolokwium", date = "11.09.2025"),
            Grade(value = "+", type = "Aktywność", date = "11.09.2025")
        )
    )
    val grades = _grades.asStateFlow()

    fun addGrade(grade: Grade) {
        _grades.update { currentList ->
            listOf(grade) + currentList // Dodajemy nową na górę listy
        }
    }

    fun updateGrade(updatedGrade: Grade) {
        _grades.update { currentList ->
            currentList.map { if (it.id == updatedGrade.id) updatedGrade else it }
        }
    }

    fun getGrade(id: String): Grade? {
        return _grades.value.find { it.id == id }
    }

    fun toggleSelection(id: String) {
        _grades.update { list ->
            list.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }
        }
    }

    fun selectGrade(id: String) {
        _grades.update { list ->
            list.map { if (it.id == id) it.copy(isSelected = true) else it }
        }
    }

    fun deleteSelected() {
        _grades.update { list -> list.filter { !it.isSelected } }
    }

    fun clearSelection() {
        _grades.update { list -> list.map { it.copy(isSelected = false) } }
    }
}

// --- 2. Główna Aktywność ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tutaj możesz dodać swój motyw (Theme)
            AppNavigation()
        }
    }
}

// --- 3. Nawigacja ---
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Pobieramy ViewModel (ten sam dla wszystkich ekranów w tym kontekście)
    val viewModel: GradesViewModel = viewModel()

    // Obserwujemy zmiany w liście ocen
    val gradesState by viewModel.grades.collectAsState()

    NavHost(navController = navController, startDestination = "grades_list") {

        // --- EKRAN 1: Lista Ocen ---
        composable("grades_list") {
            // Tutaj używamy Twojego Composable z pliku GradesScreen.kt
            // Przekazujemy mu dane i funkcje z ViewModelu
            GradesScreen(
                grades = gradesState,
                onNavigateBack = { /* Opcjonalnie finish() aktywności */ },
                onAddGradeClick = { navController.navigate("add_grade") },
                onEditGradeClick = { id -> navController.navigate("edit_grade/$id") },
                onToggleSelection = { id -> viewModel.toggleSelection(id) },
                onDeleteSelected = { viewModel.deleteSelected() },
                onClearSelection = { viewModel.clearSelection() }
            )
        }

        // --- EKRAN 2: Dodawanie Oceny ---
        composable("add_grade") {
            AddEditGradeScreen(
                gradeId = null,
                onNavigateBack = { navController.popBackStack() },
                onSave = { newGrade ->
                    viewModel.addGrade(newGrade)
                }
            )
        }

        // --- EKRAN 3: Edycja Oceny ---
        composable(
            route = "edit_grade/{gradeId}",
            arguments = listOf(navArgument("gradeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gradeId = backStackEntry.arguments?.getString("gradeId")

            // Jeśli edytujemy, musimy wstępnie wypełnić formularz danymi
            // Normalnie robi się to wewnątrz AddEditGradeScreen pobierając z VM,
            // ale dla uproszczenia Twój AddEditGradeScreen może sam sobie pobrać jeśli ma dostęp,
            // lub możemy przekazać dane startowe.

            // W tym przypadku Twój AddEditGradeScreen z poprzedniego kodu
            // próbował szukać w 'sampleGrades'.
            // Aby to działało poprawnie z ViewModelem, upewnij się, że
            // AddEditGradeScreen pobiera dane w LaunchedEffect korzystając z viewModel.getGrade(id)
            // lub przekaż istniejącą ocenę jako parametr.

            // Najprościej: Przekaż VM do AddEditGradeScreen lub przekaż obiekt Grade:
            val gradeToEdit = gradeId?.let { viewModel.getGrade(it) }

            AddEditGradeScreen(
                gradeId = gradeId,
                // Opcjonalnie przekaż tu parametry startowe, jeśli zmodyfikowałeś AddEditGradeScreen
                onNavigateBack = { navController.popBackStack() },
                onSave = { updatedGrade ->
                    viewModel.updateGrade(updatedGrade)
                }
            )
        }
    }
}