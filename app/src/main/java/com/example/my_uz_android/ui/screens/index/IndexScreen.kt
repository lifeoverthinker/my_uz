package com.example.my_uz_android.ui.screens.index

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.ui.screens.index.components.IndexTabs
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun IndexScreen(
    // Tu możesz dodać ViewModel w przyszłości: viewModel: IndexViewModel = viewModel()
    modifier: Modifier = Modifier
) {
    // Stan lokalny dla wybranej zakładki (0 = Oceny, 1 = Nieobecności)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White // Lub kolor tła z Twojego tematu
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Standardowy padding boczny
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Tytuł "Indeks"
            Text(
                text = "Indeks",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W400,
                    lineHeight = 32.sp // height 1.33 przy 24 ~= 32sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zakładki (Oceny | Nieobecności)
            IndexTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { index -> selectedTabIndex = index }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Zawartość zależna od wybranej zakładki
            when (selectedTabIndex) {
                0 -> GradesContent()
                1 -> AbsencesContent()
            }
        }
    }
}

@Composable
fun GradesContent() {
    // Placeholder dla listy ocen
    Text(
        text = "Tu pojawi się lista ocen...",
        style = TextStyle(
            fontFamily = InterFontFamily,
            color = Color.Gray
        )
    )
    // TODO: Zaimplementuj LazyColumn z ocenami
}

@Composable
fun AbsencesContent() {
    // Placeholder dla listy nieobecności
    Text(
        text = "Tu pojawi się lista nieobecności...",
        style = TextStyle(
            fontFamily = InterFontFamily,
            color = Color.Gray
        )
    )
    // TODO: Zaimplementuj LazyColumn z nieobecnościami
}