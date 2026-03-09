package com.example.my_uz_android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.MyUZTheme
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    iconRes: Int? = null,
    imageVector: ImageVector? = null,
    imageSize: Dp = 240.dp, // <--- Steruje tym, czy układ jest duży czy mały (np. na Home)
    hint: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val isCompact = imageSize <= 120.dp
    val padding = if (isCompact) 16.dp else 32.dp
    val hasIcon = iconRes != null || imageVector != null // Sprawdzamy, czy w ogóle jest ikona

    // Dodajemy zaokrąglenie i delikatne tło dla kompaktowych widoków
    val bgColor = if (isCompact) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.background
    val shape = if (isCompact) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        shape = shape
    ) {
        if (isCompact) {
            // UKŁAD KOMPAKTOWY (BEZ OBRAZKA LUB Z MAŁYM)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                if (hasIcon) {
                    when {
                        iconRes != null -> Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(imageSize)
                        )
                        imageVector != null -> Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }

                // Teksty po prawej (wyrównane do lewej)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary, // Tytuł w kolorze fioletowym
                            textAlign = TextAlign.Start
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        } else {
            // ==========================================
            // UKŁAD PIONOWY (PEŁNY EKRAN) - np. Oceny, Nieobecności
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                when {
                    iconRes != null -> Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize)
                            .padding(bottom = 8.dp)
                    )
                    imageVector != null -> Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    else -> Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }

                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                hint?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateMessagePreview() {
    MyUZTheme {
        Column {
            // Podgląd dla dużego ekranu
            EmptyStateMessage(
                title = "Duży ekran",
                message = "Wygląda na to, że nie ma tu jeszcze żadnych wpisów. Odpocznij chwilę!",
                iconRes = R.drawable.ic_launcher_foreground,
                actionText = "Odśwież",
                onActionClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Podgląd dla HomeScreen (Kompaktowy)
            EmptyStateMessage(
                title = "Karta na pulpicie",
                message = "Brak zadań na dzisiaj. Możesz iść na kawę i się zrelaksować.",
                iconRes = R.drawable.ic_launcher_foreground,
                imageSize = 80.dp // Teraz używa układu poziomego!
            )
        }
    }
}