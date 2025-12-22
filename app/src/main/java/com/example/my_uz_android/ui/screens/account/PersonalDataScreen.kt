package com.example.my_uz_android.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.components.TopAppBar
import com.example.my_uz_android.ui.theme.InterFontFamily

@Composable
fun PersonalDataScreen(
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Dane osobowe",
                navigationIcon = R.drawable.ic_chevron_left, // Poprawne ID ikony
                onNavigationClick = onNavigateBack,
                isCenterAligned = true,
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edytuj",
                            modifier = Modifier.size(24.dp), // Wymiar ikonki
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar / Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(settings?.userName ?: ""),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Imię i Nazwisko
            Text(
                text = settings?.userName?.takeIf { it.isNotBlank() } ?: "Brak danych",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Rola (Forma zwrotu)
            val roleLabel = when(settings?.gender) {
                "STUDENTKA" -> "Studentka"
                else -> "Student"
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Sekcja informacyjna
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info_circle),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp), // Wymiar ikonki
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Dane te są używane tylko wewnątrz aplikacji do personalizacji powitań.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}