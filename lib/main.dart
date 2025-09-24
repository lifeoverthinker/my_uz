import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_uz/navigation/bottom_navigation.dart';
import 'package:my_uz/theme/app_theme.dart';

// --- POCZĄTEK: IMPORTY DO PODGLĄDU KART (można usunąć po testach) ---
import 'package:my_uz/widgets/cards/class_card.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/widgets/cards/task_card.dart';
import 'package:my_uz/theme/app_colors.dart';
// --- KONIEC: IMPORTY DO PODGLĄDU KART ---

void main() {
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

  runApp(const MyUZApp());
}

/// Główna klasa aplikacji MyUZ
class MyUZApp extends StatelessWidget {
  /// Konstruktor aplikacji
  const MyUZApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MyUZ',
      theme: AppTheme.lightTheme,
      home: const HomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

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
        const ClassCard(
          title: 'Podstawy programowania obiektowego',
          time: '8:00 - 9:30',
          room: 'A-2, s. 101',
          initial: 'PO',
          backgroundColor: AppColors.myUZSysLightSecondaryContainer,
          avatarColor: AppColors.myUZSysLightPrimary,
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
          statusDotColor: AppColors.myUZExtendedCustomGreenLightColor,
        ),
        const SizedBox(height: 24),

        // --- Sekcja: TaskCard ---
        // Figma: Komponenty -> TaskCard
        Text('TaskCard', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        const TaskCard(
          title: 'Projekt zaliczeniowy',
          description: 'Przygotować aplikację mobilną zgodnie z wytycznymi.',
          initial: 'PZ',
          backgroundColor: AppColors.myUZSysLightTertiaryContainer,
          avatarColor: AppColors.myUZSysLightTertiary,
        ),
        const SizedBox(height: 16),

        // --- Sekcja: TaskCard (bez awatara) ---
        Text('TaskCard (bez awatara)', style: textTheme.headlineSmall),
        const SizedBox(height: 8),
        const TaskCard(
          title: 'Kolokwium nr 1',
          description: 'Zagadnienia z pierwszych 5 wykładów.',
          showAvatar: false,
          backgroundColor: AppColors.myUZSysLightErrorContainer,
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