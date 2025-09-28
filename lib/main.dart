import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/navigation/bottom_navigation.dart';
import 'package:my_uz/screens/onboarding/onboarding_navigator.dart';
import 'package:my_uz/theme/app_theme.dart';

// Podgląd kart (sekcja test – można usunąć po wdrożeniu)
import 'package:my_uz/widgets/cards/class_card.dart';
import 'package:my_uz/widgets/cards/event_card.dart';
import 'package:my_uz/widgets/cards/task_card.dart';

// Supabase – uproszczona konfiguracja (jeden plik w /lib)
import 'package:my_uz/supabase.dart';

/// Globalny klucz Navigatora (używany przy kończeniu onboardingu)
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // System UI (status/navigation bar) – zgodnie z designem
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
    systemNavigationBarColor: Colors.white,
    systemNavigationBarIconBrightness: Brightness.dark,
  ));

  // Inicjalizacja Supabase (klucze: --dart-define lub .env)
  try {
    await Supa.init();
  } catch (e) {
    // W DEV możesz tu dodać logger / print – fallback i tak pokaże ekran błędu w MyBootstrap
    debugPrint('[Supa][ERROR] $e');
  }

  runApp(const MyBootstrap());
}

/// Warstwa bootstrapu – asynchroniczne przygotowanie stanu aplikacji (prefs, itp.)
class MyBootstrap extends StatefulWidget {
  const MyBootstrap({super.key});

  @override
  State<MyBootstrap> createState() => _MyBootstrapState();
}

class _MyBootstrapState extends State<MyBootstrap> {
  bool _ready = false;
  bool _error = false;
  bool _onboardingComplete = false;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    setState(() {
      _ready = false;
      _error = false;
    });

    try {
      final prefs = await SharedPreferences.getInstance();
      _onboardingComplete = prefs.getBool('onboarding_complete') ?? false;
      if (!mounted) return;
      setState(() => _ready = true);
    } catch (e) {
      if (!mounted) return;
      setState(() => _error = true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MyUZ',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      navigatorKey: navigatorKey,
      home: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_error) {
      return Scaffold(
        body: Center(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.error_outline, size: 48, color: Colors.redAccent),
                const SizedBox(height: 16),
                const Text(
                  'Błąd inicjalizacji',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                const Text(
                  'Nie udało się przygotować aplikacji. Spróbuj ponownie.',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                FilledButton(
                  onPressed: _init,
                  child: const Text('Spróbuj ponownie'),
                ),
              ],
            ),
          ),
        ),
      );
    }

    if (!_ready) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    // Zwracamy bezpośrednio właściwy ekran zamiast tworzyć drugie MaterialApp
    return _onboardingComplete
        ? const HomePage()
        : OnboardingNavigator(
            onFinishOnboarding: () async {
              final prefs = await SharedPreferences.getInstance();
              await prefs.setBool('onboarding_complete', true);
              navigatorKey.currentState?.pushReplacement(
                MaterialPageRoute(builder: (_) => const HomePage()),
              );
            },
          );
  }
}

/// Usunięto wewnętrzne MaterialApp – klasa pozostawiona opcjonalnie dla testów jako prosty wrapper (bez navigatorKey)
class MyUZApp extends StatelessWidget {
  final bool onboardingComplete;
  const MyUZApp({super.key, required this.onboardingComplete});

  @override
  Widget build(BuildContext context) {
    // Jeśli aplikacja jest uruchamiana bez MyBootstrap (np. w testach), otulamy minimalnym MaterialApp,
    // aby nie powielać navigatorKey produkcyjnego.
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: onboardingComplete
          ? const HomePage()
          : OnboardingNavigator(
              onFinishOnboarding: () async {
                final prefs = await SharedPreferences.getInstance();
                await prefs.setBool('onboarding_complete', true);
                navigatorKey.currentState?.pushReplacement(
                  MaterialPageRoute(builder: (_) => const HomePage()),
                );
              },
            ),
    );
  }
}

/// Strona główna z bottom navigation
class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _selectedIndex = 0;

  // Tymczasowe ekrany (dashboard + placeholdery)
  final List<Widget> _pages = const [
    MainDashboard(),
    PlaceholderPage(title: 'Kalendarz'),
    PlaceholderPage(title: 'Indeks'),
    PlaceholderPage(title: 'Konto'),
  ];

  static const List<String> _titles = ['Strona Główna', 'Kalendarz', 'Indeks', 'Konto'];

  void _onItemTapped(int idx) => setState(() => _selectedIndex = idx);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _selectedIndex == 0 ? AppBar(title: Text(_titles[_selectedIndex])) : null,
      body: _pages[_selectedIndex],
      bottomNavigationBar: MyUZBottomNavigation(
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
      ),
    );
  }
}

/// Dashboard – podgląd kart (sekcja dev)
class MainDashboard extends StatelessWidget {
  const MainDashboard({super.key});

  @override
  Widget build(BuildContext context) {
    final tt = Theme.of(context).textTheme;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('ClassCard', style: tt.headlineSmall),
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
        Text('ClassCard.calendar', style: tt.headlineSmall),
        const SizedBox(height: 8),
        ClassCard.calendar(
          title: 'Analiza matematyczna II',
          time: '10:00 - 11:30',
          room: 'A-29, s. 305',
          statusDotColor: const Color(0xFF66BB6A),
        ),
        const SizedBox(height: 24),
        Text('TaskCard', style: tt.headlineSmall),
        const SizedBox(height: 8),
        TaskCard(
          title: 'Projekt zaliczeniowy',
          description: 'Przygotować aplikację mobilną zgodnie z wytycznymi.',
          initial: 'PZ',
          backgroundColor: Theme.of(context).colorScheme.tertiaryContainer,
          avatarColor: Theme.of(context).colorScheme.tertiary,
        ),
        const SizedBox(height: 16),
        Text('TaskCard (bez awatara)', style: tt.headlineSmall),
        const SizedBox(height: 8),
        TaskCard(
          title: 'Kolokwium nr 1',
          description: 'Zagadnienia z pierwszych 5 wykładów.',
          showAvatar: false,
          backgroundColor: Theme.of(context).colorScheme.errorContainer,
        ),
        const SizedBox(height: 24),
        Text('EventCard', style: tt.headlineSmall),
        const SizedBox(height: 8),
        const EventCard(
          title: 'Juwenalia 2025',
          description: 'Koncerty, grill i inne atrakcje na kampusie A i B.',
          backgroundColor: Color(0xFFDAF5D7),
        ),
      ],
    );
  }
}

/// Prosty placeholder dla niezaimplementowanych ekranów
class PlaceholderPage extends StatelessWidget {
  final String title;
  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    final tt = Theme.of(context).textTheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(title, style: tt.headlineMedium),
            const SizedBox(height: 16),
            Text(
              'Ta funkcjonalność jest w trakcie implementacji',
              style: tt.bodyLarge,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

