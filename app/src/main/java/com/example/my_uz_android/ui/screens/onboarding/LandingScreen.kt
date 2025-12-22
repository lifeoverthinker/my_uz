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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onFinishOnboarding: () -> Unit = { onNavigateToHome() }
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val totalPages = viewModel.totalPages
    val isLoading by viewModel.isLoading.collectAsState()

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isKeyboardVisible = remember(imeInsets) { imeInsets.getBottom(density) > 0 }

    MyUZTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Puste - konfiguracja obowiązkowa
                }
            },
            bottomBar = {
                if (!isKeyboardVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PageIndicators(totalPages = totalPages, currentPage = currentPage)
                        Spacer(Modifier.height(24.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            when (currentPage) {
                                0 -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Button(
                                            onClick = { viewModel.onNextClick() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("Rozpocznij", style = MaterialTheme.typography.labelLarge)
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_chevron_right),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                5 -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = { viewModel.onBackClick() },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            enabled = !isLoading
                                        ) {
                                            Text("Wstecz")
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.saveOnboardingData { onFinishOnboarding() }
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            enabled = !isLoading
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            } else {
                                                Text("Gotowe!")
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    // Logika blokowania przycisku "Dalej"
                                    val isNextEnabled = when(currentPage) {
                                        1 -> selectedGender != null && userName.isNotBlank()
                                        2 -> !selectedGroup.isNullOrBlank()
                                        else -> true
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = { viewModel.onBackClick() },
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Icon(painterResource(R.drawable.ic_chevron_left), null, Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Wstecz")
                                        }
                                        Button(
                                            onClick = { viewModel.onNextClick() },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            enabled = isNextEnabled
                                        ) {
                                            Text("Dalej")
                                            Spacer(Modifier.width(8.dp))
                                            Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        FooterText()
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
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
                        0 -> WelcomeStepContent()
                        1 -> PersonalizationStepContent(viewModel)
                        2 -> GroupSelectionStepContent(viewModel)
                        3 -> CalendarFeatureStepContent()
                        4 -> GradesFeatureStepContent()
                        5 -> MapFeatureStepContent()
                    }
                }
            }
        }
    }
}

// --- EKRANY ZAWARTOŚCI ---

@Composable
fun WelcomeStepContent() {
    ResponsiveOnboardingStep(illustrationResId = R.drawable.college_students_rafiki) { // Zakładam, że getIllustrationResId(0) to ten obrazek
        OnboardingTexts(
            title = "Witaj w MyUZ! 👋",
            subtitle = "Twój cyfrowy asystent",
            description = "Plan zajęć, oceny i mapa kampusu w jednym miejscu."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationStepContent(viewModel: OnboardingViewModel) {
    val selectedGender by viewModel.selectedGender.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userSurname by viewModel.userSurname.collectAsState()
    val focusManager = LocalFocusManager.current

    ResponsiveOnboardingStep(illustrationResId = R.drawable.hello_rafiki) {
        OnboardingTexts(
            title = "Personalizacja",
            subtitle = "Kim jesteś?",
            description = "Dzięki temu aplikacja będzie zwracać się do Ciebie tak, jak lubisz."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Forma zwrotu - styl spójny z wyborem grupy
        Text(
            text = "Forma zwrotu",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChip(
                selected = selectedGender == UserGender.STUDENT,
                onClick = { viewModel.setGender(UserGender.STUDENT) },
                label = {
                    Text(
                        "Student",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.weight(1f),
                leadingIcon = null
            )

            FilterChip(
                selected = selectedGender == UserGender.STUDENTKA,
                onClick = { viewModel.setGender(UserGender.STUDENTKA) },
                label = {
                    Text(
                        "Studentka",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.weight(1f),
                leadingIcon = null
            )
        }

        // Dane osobowe
        AnimatedVisibility(
            visible = selectedGender != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Twoje dane",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { viewModel.setUserName(it) },
                    label = { Text("Imię") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userSurname,
                    onValueChange = { viewModel.setUserSurname(it) },
                    label = { Text("Nazwisko (opcjonalne)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }
    }
}

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

    ResponsiveOnboardingStep(illustrationResId = R.drawable.settings_rafiki) {
        OnboardingTexts(
            title = "Twoja Grupa",
            subtitle = "Pobierz plan zajęć",
            description = "Wpisz kod grupy dziekańskiej (np. 32INF-SP)."
        )

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
                    placeholder = { Text("Szukaj grupy...") },
                    label = { Text("Kod grupy") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, Modifier.size(24.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setGroupSearchQuery("") }) {
                                Icon(painterResource(R.drawable.ic_x_close), "Wyczyść", Modifier.size(24.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                ExposedDropdownMenu(
                    expanded = expanded && filteredGroups.isNotEmpty(),
                    onDismissRequest = { expanded = false }
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
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            if (!isLoading && searchQuery.isNotEmpty() && filteredGroups.isEmpty() && selectedGroup == null) {
                Text(
                    text = "Nie znaleziono takiej grupy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }

        AnimatedVisibility(visible = selectedGroup != null && availableSubgroups.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // Spójny odstęp
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz podgrupy",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.Center) {
                    availableSubgroups.forEach { subgroup ->
                        FilterChip(
                            selected = selectedSubgroups.contains(subgroup),
                            onClick = { viewModel.toggleSubgroup(subgroup) },
                            label = { Text(subgroup) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarFeatureStepContent() {
    InfoStepContent(3, "Terminarz", "Zarządzaj czasem", "Twój plan zajęć i osobiste notatki w przejrzystym kalendarzu.")
}

@Composable
fun GradesFeatureStepContent() {
    InfoStepContent(4, "Indeks", "Twoje postępy", "Monitoruj swoje oceny i średnią na bieżąco.")
}

@Composable
fun MapFeatureStepContent() {
    InfoStepContent(5, "Mapa Kampusu", "Nawigacja", "Znajdź każdą salę i budynek na terenie kampusu.")
}

@Composable
fun InfoStepContent(pageIndex: Int, title: String, subtitle: String, description: String) {
    // pageIndex mapuje do zasobu w getIllustrationResId
    val resId = when(pageIndex) {
        3 -> R.drawable.calendar_rafiki
        4 -> R.drawable.grades_rafiki
        5 -> R.drawable.paper_map_rafiki
        else -> R.drawable.ic_user
    }
    ResponsiveOnboardingStep(illustrationResId = resId) {
        OnboardingTexts(title, subtitle, description)
    }
}

// --- WSPÓLNE HELPERY ---

@Composable
fun ResponsiveOnboardingStep(
    illustrationResId: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(min = 200.dp, max = 350.dp)
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = illustrationResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}

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
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
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
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun PageIndicators(totalPages: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val width = if (isActive) 24.dp else 8.dp
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
        text = "MyUZ 2025 v1.0.0",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
    )
}