package com.example.my_uz_android.ui.screens.account

/**
 * Ekran z informacjami o aplikacji, jej autorze (Projekt Inżynierski)
 * oraz podziękowaniami za użyte licencje (np. grafiki Storyset).
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.components.TopAppBar
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(onBackClick: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.about_app_title),
                navigationIcon = R.drawable.ic_chevron_left,
                onNavigationClick = onBackClick,
                isNavigationIconFilled = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- LOGO APKI ---
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.logo_content_description),
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- INFORMACJE O PROJEKCIE (INŻYNIERKA) ---
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Twój Cyfrowy Asystent", // Albo z zasobów: stringResource(R.string.about_app_subtitle)
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Tutaj zmieniamy tekst na jawnie mówiący o projekcie niezależnym!
                    Text(
                        text = "MyUZ to niezależna aplikacja stworzona od podstaw w ramach projektu inżynierskiego przez jednego studenta. Jej celem jest ułatwienie życia akademickiego na Uniwersytecie Zielonogórskim poprzez zintegrowanie planu zajęć, ocen i powiadomień w jednym, nowoczesnym miejscu.\n\nAplikacja nie jest oficjalnym produktem uczelni, lecz studencką inicjatywą zrodzoną z pasji do programowania.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- PODZIĘKOWANIA / LICENCJE ---
                StorysetAttribution()

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ikony: SVG Repo (Licencja CC0)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- STOPKA AUTORSKA ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Projekt Inżynierski 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Autor: Martyna",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Komponent odpowiedzialny za wyświetlenie klikalnego linku do autorów ilustracji.
 */
@Composable
fun StorysetAttribution() {
    val uriHandler = LocalUriHandler.current

    val prefix = "Ilustracje od "
    val storysetLabel = "Storyset"

    val annotatedLinkString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append(prefix)
        }
        pushStringAnnotation(tag = "storyset", annotation = "https://storyset.com/online")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(storysetLabel)
        }
        pop()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ClickableText(
            text = annotatedLinkString,
            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
            onClick = { offset ->
                annotatedLinkString.getStringAnnotations(tag = "storyset", start = offset, end = offset)
                    .firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
            }
        )
    }
}