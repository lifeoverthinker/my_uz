import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_uz/navigation/bottom_navigation.dart';
import 'package:my_uz/screens/onboarding/onboarding_navigator.dart';
import 'package:my_uz/theme/app_theme.dart';
import 'package:shared_preferences/shared_preferences.dart';

// --- POCZĄTEK: IMPORTY DO PODGLĄDU KART (można usunąć po testach) ---
import 'package:my_uz/widgets/cards/class_card.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
// --- KONIEC: IMPORTY DO PODGLĄDU KART ---

void main() async {
  // Upewniamy się, że wiązania Flutter zostały zainicjalizowane
  WidgetsFlutterBinding.ensureInitialized();

  // Ustawiamy kolor paska systemowego na biały
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent, // Przezroczysty status bar
    statusBarIconBrightness: Brightness.dark, // Ciemne ikony na białym tle
    systemNavigationBarColor: Colors.white, // Biały pasek systemowy
    systemNavigationBarIconBrightness:
    Brightness.dark, // Ciemne ikony na pasku systemowym
  ));

  final prefs = await SharedPreferences.getInstance();
  final onboardingComplete = prefs.getBool('onboarding_complete') ?? false;

  runApp(MyUZApp(onboardingComplete: onboardingComplete));
}

/// Główna klasa aplikacji MyUZ
class MyUZApp extends StatelessWidget {
  final bool onboardingComplete;
  /// Konstruktor aplikacji
  const MyUZApp({super.key, required this.onboardingComplete});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MyUZ',
      theme: AppTheme.lightTheme,
      home: onboardingComplete ? const HomePage() : OnboardingNavigator(
        onFinishOnboarding: () async {
          final prefs = await SharedPreferences.getInstance();
          await prefs.setBool('onboarding_complete', true);
          // Po zakończeniu onboardingu, przechodzimy do HomePage.
          // Używamy globalnego klucza nawigacyjnego, aby uzyskać dostęp do kontekstu.
          navigatorKey.currentState?.pushReplacement(
            MaterialPageRoute(builder: (_) => const HomePage()),
          );
        },
      ),
      debugShowCheckedModeBanner: false,
      navigatorKey: navigatorKey, // Ustawienie globalnego klucza nawigacyjnego
    );
  }
}

// Globalny klucz do nawigacji, aby można było nawigować bez kontekstu build.
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();


/// Strona główna aplikacji z nawigacją
class HomePage extends StatefulWidget {
  /// Konstruktor strony głównej
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // Indeks aktualnie wybranej zakładki
  int _selectedIndex = 0;

  // --- POCZĄTEK: KOD TYMCZASOWY DO PODGLĄDU KART ---
  // Aby ukryć podgląd, zakomentuj tę listę i odkomentuj "ORYGINALNY KOD" poniżej.
  final List<Widget> _pages = [
    const MainDashboard(), // Nowy widok strony głównej z kartami
    const PlaceholderPage(title: 'Kalendarz'),
    const PlaceholderPage(title: 'Indeks'),
    const PlaceholderPage(title: 'Konto'),
  ];
  // --- KONIEC: KOD TYMCZASOWY DO PODGLĄDU KART ---

  // --- POCZĄTEK: ORYGINALNY KOD ---
  // Aby przywrócić oryginalny wygląd, odkomentuj tę listę i zakomentuj
  // "KOD TYMCZASOWY DO PODGLĄDU KART" powyżej.
  // final List<Widget> _pages = [
  //   const PlaceholderPage(title: 'Strona Główna'),
  //   const PlaceholderPage(title: 'Kalendarz'),
  //   const PlaceholderPage(title: 'Indeks'),
  //   const PlaceholderPage(title: 'Konto'),
  // ];
  // --- KONIEC: ORYGINALNY KOD ---

  // Metoda zmiany wybranej zakładki
  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    // Lista tytułów dla AppBar
    const List<String> titles = ['Strona Główna', 'Kalendarz', 'Indeks', 'Konto'];

    return Scaffold(
      // AppBar będzie widoczny tylko na stronie głównej (indeks 0).
      // Na pozostałych ekranach będzie ukryty.
      appBar: _selectedIndex == 0
          ? AppBar(
        title: Text(titles[_selectedIndex]),
      )
          : null,
      body: _pages[_selectedIndex],
      bottomNavigationBar: MyUZBottomNavigation(
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
      ),
    );
  }
}

// --- POCZĄTEK: WIDŻET TYMCZASOWY DO PODGLĄDU KART ---
// Ten widżet można usunąć lub zakomentować razem z jego użyciem w _pages.
/// Widżet strony głównej wyświetlający podgląd kart.
class MainDashboard extends StatelessWidget {
  const MainDashboard({super.key});

  @override
  Widget build(BuildContext context) {
    // Pobieramy style tekstu z motywu aplikacji, co jest zgodne z zasadą DRY.
    final textTheme = Theme.of(context).textTheme;

    // Używamy ListView, aby umożliwić przewijanie, gdyby karty
    // nie mieściły się na ekranie.
    // Usunięto 'const' z listy, aby naprawić błąd kompilacji.
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        // --- Sekcja: ClassCard ---
        // Figma: Komponenty -> ClassCard
        Text('ClassCard', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        ClassCard(
          title: 'Podstawy programowania obiektowego',
          time: '8:00 - 9:30',
          room: 'A-2, s. 101',
          initial: 'PO',
          backgroundColor: Theme.of(context).colorScheme.secondaryContainer,
          avatarColor: Theme.of(context).colorScheme.primary,
        ),
        const SizedBox(height: 16),

        // --- Sekcja: ClassCard.calendar ---
        // Figma: Komponenty -> ClassCard (wariant kalendarza)
        Text('ClassCard.calendar', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        // Konstruktor fabryczny .calendar() nie może być 'const'
        ClassCard.calendar(
          title: 'Analiza matematyczna II',
          time: '10:00 - 11:30',
          room: 'A-29, s. 305',
          statusDotColor: const Color(0xFF66BB6A), // Przykładowy kolor
        ),
        const SizedBox(height: 24),

        // --- Sekcja: TaskCard ---
        // Figma: Komponenty -> TaskCard
        Text('TaskCard', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        TaskCard(
          title: 'Projekt zaliczeniowy',
          description: 'Przygotować aplikację mobilną zgodnie z wytycznymi.',
          initial: 'PZ',
          backgroundColor: Theme.of(context).colorScheme.tertiaryContainer,
          avatarColor: Theme.of(context).colorScheme.tertiary,
        ),
        const SizedBox(height: 16),

        // --- Sekcja: TaskCard (bez awatara) ---
        Text('TaskCard (bez awatara)', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        TaskCard(
          title: 'Kolokwium nr 1',
          description: 'Zagadnienia z pierwszych 5 wykładów.',
          showAvatar: false,
          backgroundColor: Theme.of(context).colorScheme.errorContainer,
        ),
        const SizedBox(height: 24),

        // --- Sekcja: EventCard ---
        // Figma: Komponenty -> EventCard
        Text('EventCard', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        const EventCard(
          title: 'Juwenalia 2025',
          description: 'Koncerty, grill i inne atrakcje na kampusie A i B.',
          // Zgodnie z kodem karty, ten kolor jest używany jako domyślny,
          // ale dla czytelności dodajemy go tutaj explicite.
          backgroundColor: Color(0xFFDAF5D7),
        ),
      ],
    );
  }
}
// --- KONIEC: WIDŻET TYMCZASOWY DO PODGLĄDU KART ---

/// Tymczasowa strona zastępcza dla widoków w rozwoju
class PlaceholderPage extends StatelessWidget {
  /// Tytuł strony
  final String title;

  /// Konstruktor strony zastępczej
  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              title,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 16),
            Text(
              'Ta funkcjonalność jest w trakcie implementacji',
              style: Theme.of(context).textTheme.bodyLarge,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}