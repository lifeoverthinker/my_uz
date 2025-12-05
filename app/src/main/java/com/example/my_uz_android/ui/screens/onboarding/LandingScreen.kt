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
    onFinishOnboarding: () -> Unit = {}
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages = viewModel.totalPages
    val isLoading by viewModel.isLoading.collectAsState() // Dodano obserwację ładowania

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
                    if (currentPage < 5) {
                        TextButton(
                            onClick = {
                                viewModel.skipOnboarding()
                                onFinishOnboarding()
                            }
                        ) {
                            Text("Pomiń", style = MaterialTheme.typography.labelLarge)
                        }
                    }
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
                                                // POPRAWKA: Czekamy na zakończenie zapisu
                                                viewModel.saveOnboardingData {
                                                    onFinishOnboarding()
                                                }
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = { viewModel.onBackClick() },
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.ic_chevron_left),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Wstecz")
                                        }
                                        Button(
                                            onClick = { viewModel.onNextClick() },
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Text("Dalej")
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                painterResource(R.drawable.ic_chevron_right),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
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
fun WelcomeStepContent() {
    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(0)) {
        OnboardingTexts(
            title = "Witaj w MyUZ! 👋",
            subtitle = "Twój cyfrowy asystent",
            description = "Plan zajęć, oceny i mapa kampusu w jednym miejscu."
        )
    }
}

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
            subtitle = "Przedstaw się nam",
            description = "Wybierz tryb anonimowy lub podaj swoje dane."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModeSelectionCard(
                title = "Anonimowy",
                subtitle = "Bez zapisu",
                isSelected = selectedMode == OnboardingMode.ANONYMOUS,
                onClick = {
                    viewModel.setMode(OnboardingMode.ANONYMOUS)
                    focusManager.clearFocus()
                },
                modifier = Modifier.weight(1f)
            )
            ModeSelectionCard(
                title = "Student",
                subtitle = "Pełne funkcje",
                isSelected = selectedMode == OnboardingMode.DATA,
                onClick = { viewModel.setMode(OnboardingMode.DATA) },
                modifier = Modifier.weight(1f)
            )
        }

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

    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(2)) {
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
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
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
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz podgrupy (opcjonalne):",
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
    ResponsiveOnboardingStep(illustrationResId = getIllustrationResId(pageIndex)) {
        OnboardingTexts(title, subtitle, description)
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
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}