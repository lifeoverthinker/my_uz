package com.example.my_uz_android.ui.components

/**
 * Komponent karty zadania wykorzystywany na ekranach dashboardu i list zadań.
 * Udostępnia spójny sposób prezentacji tytułu, przedmiotu i stanu realizacji
 * z zachowaniem zgodności wizualnej z pozostałymi kartami aplikacji.
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.data.models.TaskEntity
import com.example.my_uz_android.ui.theme.InterFontFamily

/**
 * Komponent wyświetlający zadanie (Task) ze statusem ukończenia i terminem.
 * Budowa i paddingi identyczne jak w EventCard.
 *
 * @param task Dane zadania do wyświetlenia.
 * @param onTaskClick Akcja wykonywana po kliknięciu karty.
 * @param modifier Modyfikator układu Compose.
 * @param backgroundColor Opcjonalny kolor tła karty.
 * @param isDarkMode Flaga określająca użycie kolorów trybu ciemnego.
 */
@Composable
fun TaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val isCompleted = task.isCompleted
    
    // Domyślny kolor niebieski dla zadań
    val baseBackgroundColor = backgroundColor ?: if (isDarkMode) Color(0xFF233436) else Color(0xFFD8FCFF)

    val cardBackgroundColor = if (isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        baseBackgroundColor
    }

    // Kolory tekstu dostosowane do trybu
    val titleColor = MaterialTheme.colorScheme.onSurface
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp) // Taka sama wysokość minimalna jak EventCard
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackgroundColor)
            .clickable { onTaskClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Identyczne rozmieszczenie jak w EventCard
    ) {
        Text(
            text = task.title,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight(500),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = titleColor,
                textDecoration = textDecoration
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        val subjectText = task.subjectName?.takeIf { it.isNotBlank() }
        if (subjectText != null) {
            Text(
                text = subjectText,
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight(400),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = secondaryColor
                ),
                maxLines = 2, // Pozwalamy na 2 linie, by wysokość była spójna z EventCard (który ma 2 linie opisu)
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
