import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_uz/navigation/bottom_navigation.dart';
import 'package:my_uz/theme/app_theme.dart';

void main() {
  // Upewniamy się, że wiązania Flutter zostały zainicjalizowane
  WidgetsFlutterBinding.ensureInitialized();

  // Ustawiamy kolor paska systemowego na biały
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent, // Przezroczysty status bar
    statusBarIconBrightness: Brightness.dark, // Ciemne ikony na białym tle
    systemNavigationBarColor: Colors.white, // Biały pasek systemowy
    systemNavigationBarIconBrightness: Brightness.dark, // Ciemne ikony na pasku systemowym
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

  // Lista widoków dla każdej zakładki
  final List<Widget> _pages = [
    const PlaceholderPage(title: 'Strona Główna'),
    const PlaceholderPage(title: 'Kalendarz'),
    const PlaceholderPage(title: 'Indeks'),
    const PlaceholderPage(title: 'Konto'),
  ];

  // Metoda zmiany wybranej zakładki
  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('MyUZ'),
      ),
      body: _pages[_selectedIndex],
      bottomNavigationBar: MyUZBottomNavigation(
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
      ),
    );
  }
}

/// Tymczasowa strona zastępcza dla widoków w rozwoju
class PlaceholderPage extends StatelessWidget {
  /// Tytuł strony
  final String title;

  /// Konstruktor strony zastępczej
  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Center(
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
    );
  }
}