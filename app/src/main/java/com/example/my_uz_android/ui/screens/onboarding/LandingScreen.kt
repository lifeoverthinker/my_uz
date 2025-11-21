package com.example.my_uz_android.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.AppViewModelProvider
import com.example.my_uz_android.ui.theme.MyUZTheme

@Composable
private fun getIllustrationResId(currentPage: Int): Int = when (currentPage) {
    0 -> R.drawable.college_students_rafiki
    1 -> R.drawable.hello_rafiki
    2 -> R.drawable.settings_rafiki
    3 -> R.drawable.calendar_rafiki
    4 -> R.drawable.grades_rafiki
    5 -> R.drawable.paper_map_rafiki
    else -> R.drawable.ic_user
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFinishOnboarding: () -> Unit = {}
) {
    val currentPage by viewModel.currentPage.collectAsState()

    MyUZTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(), // Kluczowe dla skalowania przy klawiaturze
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Przycisk Pomiń widoczny tylko jeśli nie jesteśmy na ostatnim ekranie
                    if (currentPage < 5) {
                        TextButton(onClick = onFinishOnboarding) {
                            Text("Pomiń", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp) // 8-grid (16dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    FooterText()
                }
            }
        ) { innerPadding ->
            // Główny kontener z paddingami
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp), // 8-grid (24dp margins)
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally(animationSpec = tween(400)) { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = tween(400)) { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "OnboardingContent"
                ) { page ->
                    when (page) {
                        0 -> WelcomeStepContent(viewModel)
                        1 -> PersonalizationStepContent(viewModel)
                        2 -> GroupSelectionStepContent(viewModel)
                        3 -> CalendarFeatureStepContent(viewModel)
                        4 -> GradesFeatureStepContent(viewModel)
                        5 -> MapFeatureStepContent(
                            viewModel = viewModel,
                            onFinishOnboarding = {
                                viewModel.saveOnboardingData()
                                onFinishOnboarding()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wrapper dla każdego kroku onboardingu zapewniający spójny layout i skalowanie ilustracji.
 * @param illustrationContent Obrazek, który wypełni górną przestrzeń (weight 1f).
 * @param bottomContent Treść (teksty, pola, przyciski), która jest "sztywna" na dole.
 */
@Composable
fun ResponsiveOnboardingStep(
    illustrationResId: Int,
    bottomContent: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Sekcja Ilustracji - Elastyczna (weight 1f)
        // Dzięki temu obrazek zajmuje całe wolne miejsce i kurczy się, gdy dół rośnie (np. przez klawiaturę)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Odstęp góra/dół od obrazka
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = illustrationResId),
                contentDescription = null,
                contentScale = ContentScale.Fit, // Skaluje proporcjonalnie wewnątrz Boxa
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Sekcja Treści - Dolna
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Odstęp od stopki
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // 8-grid spacing
        ) {
            bottomContent()
        }
    }
}

// --- EKRAN 0: WITAJ ---
@Composable
fun WelcomeStepContent(viewModel: OnboardingViewModel) {
    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(0)) {
        OnboardingTexts(
            title = "Witaj w MyUZ! 👋",
            subtitle = "Twój cyfrowy asystent na Uniwersytecie",
            description = "Zarządzaj zajęciami, zadaniami i ocenami w jednym miejscu."
        )
        Spacer(Modifier.height(16.dp)) // 8-grid
        PageIndicators(totalPages = viewModel.totalPages, currentPage = 0)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.onNextClick() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Rozpocznij", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Dalej",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- EKRAN 1: PERSONALIZACJA ---
@Composable
fun PersonalizationStepContent(viewModel: OnboardingViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userSurname by viewModel.userSurname.collectAsState()
    val focusManager = LocalFocusManager.current

    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(1)) {
        OnboardingTexts(
            title = "Personalizacja",
            subtitle = "Jak mamy się do Ciebie zwracać?",
            description = "Wybierz tryb anonimowy lub wprowadź swoje dane"
        )

        // Karty wyboru trybu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModeSelectionCard(
                title = "Anonimowy",
                subtitle = "Bez danych",
                isSelected = selectedMode == OnboardingMode.ANONYMOUS,
                onClick = {
                    viewModel.setMode(OnboardingMode.ANONYMOUS)
                    focusManager.clearFocus()
                },
                modifier = Modifier.weight(1f)
            )
            ModeSelectionCard(
                title = "Student",
                subtitle = "Podaj imię",
                isSelected = selectedMode == OnboardingMode.DATA,
                onClick = { viewModel.setMode(OnboardingMode.DATA) },
                modifier = Modifier.weight(1f)
            )
        }

        // Pola zależne od wyboru
        // Używamy AnimatedVisibility, layout sam się przeliczy
        AnimatedVisibility(visible = selectedMode == OnboardingMode.ANONYMOUS) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = selectedGender == UserGender.STUDENT,
                    onClick = { viewModel.setGender(UserGender.STUDENT) },
                    label = { Text("Student") }
                )
                FilterChip(
                    selected = selectedGender == UserGender.STUDENTKA,
                    onClick = { viewModel.setGender(UserGender.STUDENTKA) },
                    label = { Text("Studentka") }
                )
            }
        }

        AnimatedVisibility(visible = selectedMode == OnboardingMode.DATA) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { viewModel.setUserName(it) },
                    label = { Text("Imię") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = userSurname,
                    onValueChange = { viewModel.setUserSurname(it) },
                    label = { Text("Nazwisko") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        PageIndicators(totalPages = viewModel.totalPages, currentPage = 1)
        OnboardingNavigationButtons(onBack = { viewModel.onBackClick() }, onNext = { viewModel.onNextClick() })
    }
}

// --- EKRAN 2: WYBÓR GRUPY ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupSelectionStepContent(viewModel: OnboardingViewModel) {
    val searchQuery by viewModel.groupSearchQuery.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val availableSubgroups by viewModel.availableSubgroups.collectAsState()
    val selectedSubgroups by viewModel.selectedSubgroups.collectAsState()
    val filteredGroups by viewModel.filteredGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(2)) {
        OnboardingTexts(
            title = "Twoja Grupa",
            subtitle = "Znajdź swój plan zajęć",
            description = "Wpisz kod grupy aby wyszukać"
        )

        // Wyszukiwarka
        Column(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded && filteredGroups.isNotEmpty(),
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setGroupSearchQuery(it)
                        expanded = true
                    },
                    placeholder = { Text("np. 32INF-SP") },
                    label = { Text("Kod grupy") },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search),
                            contentDescription = "Szukaj",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setGroupSearchQuery("") }) {
                                Icon(
                                    painterResource(R.drawable.ic_x_close),
                                    contentDescription = "Wyczyść",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded && filteredGroups.isNotEmpty(),
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    filteredGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = {
                                viewModel.selectGroup(group)
                                expanded = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!isLoading && searchQuery.isNotEmpty() && filteredGroups.isEmpty() && selectedGroup == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nie znaleziono takiej grupy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Wybór podgrup
        AnimatedVisibility(visible = selectedGroup != null && availableSubgroups.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz podgrupy (opcjonalne):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSubgroups.forEach { subgroup ->
                        FilterChip(
                            selected = selectedSubgroups.contains(subgroup),
                            onClick = { viewModel.toggleSubgroup(subgroup) },
                            label = { Text(subgroup) },
                            leadingIcon = if (selectedSubgroups.contains(subgroup)) {
                                {
                                    Icon(
                                        painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        PageIndicators(totalPages = viewModel.totalPages, currentPage = 2)
        OnboardingNavigationButtons(onBack = { viewModel.onBackClick() }, onNext = { viewModel.onNextClick() })
    }
}

// --- EKRANY INFORMACYJNE (3, 4, 5) ---

@Composable
fun CalendarFeatureStepContent(viewModel: OnboardingViewModel) {
    InfoStepContent(
        viewModel = viewModel,
        pageIndex = 3,
        title = "Terminarz",
        subtitle = "Wszystkie zajęcia w jednym miejscu",
        description = "Sprawdzaj plan zajęć, dodawaj zadania i nie przegap żadnego wydarzenia."
    )
}

@Composable
fun GradesFeatureStepContent(viewModel: OnboardingViewModel) {
    InfoStepContent(
        viewModel = viewModel,
        pageIndex = 4,
        title = "Indeks",
        subtitle = "Śledź swoje postępy",
        description = "Twoje oceny, średnia i osiągnięcia zawsze pod ręką w Twoim telefonie."
    )
}

@Composable
fun MapFeatureStepContent(viewModel: OnboardingViewModel, onFinishOnboarding: () -> Unit) {
    InfoStepContent(
        viewModel = viewModel,
        pageIndex = 5,
        title = "Mapa Kampusu",
        subtitle = "Nigdy się nie zgub",
        description = "Interaktywna mapa pomoże Ci znaleźć sale wykładowe i budynki uczelni.",
        isFinalStep = true,
        onFinalAction = onFinishOnboarding
    )
}

// Helper dla prostych ekranów informacyjnych
@Composable
fun InfoStepContent(
    viewModel: OnboardingViewModel,
    pageIndex: Int,
    title: String,
    subtitle: String,
    description: String,
    isFinalStep: Boolean = false,
    onFinalAction: () -> Unit = {}
) {
    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(pageIndex)) {
        OnboardingTexts(title, subtitle, description)
        Spacer(Modifier.height(16.dp))
        PageIndicators(totalPages = viewModel.totalPages, currentPage = pageIndex)
        Spacer(Modifier.height(8.dp))

        if (isFinalStep) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.onBackClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "Wstecz",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Wstecz")
                }
                Button(
                    onClick = onFinalAction,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Gotowe!")
                }
            }
        } else {
            OnboardingNavigationButtons(onBack = { viewModel.onBackClick() }, onNext = { viewModel.onNextClick() })
        }
    }
}

// --- WSPÓLNE KOMPONENTY ---

@Composable
fun OnboardingTexts(title: String, subtitle: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun PageIndicators(totalPages: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val width = if (isActive) 24.dp else 8.dp // 8-grid sizing
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
                    .animateContentSize()
            )
        }
    }
}

@Composable
fun FooterText() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("MyUZ 2025\n") }
            append("v1.0.0")
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun OnboardingNavigationButtons(onBack: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_chevron_left),
                contentDescription = "Wstecz",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Wstecz")
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text("Dalej")
            Spacer(Modifier.width(8.dp))
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = "Dalej",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModeSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}