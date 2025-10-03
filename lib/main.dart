import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/navigation/bottom_navigation.dart';
import 'package:my_uz/screens/onboarding/onboarding_navigator.dart';
import 'package:my_uz/theme/app_theme.dart';
import 'package:my_uz/screens/home/home_screen.dart';
import 'package:my_uz/screens/index/index_screen.dart';
import 'package:my_uz/supabase.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
    systemNavigationBarColor: Colors.white,
    systemNavigationBarIconBrightness: Brightness.dark,
  ));

  try {
    await Supa.init();
  } catch (e) {
    debugPrint('[Supa][ERROR] $e');
  }

  runApp(const MyBootstrap());
}

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
    } catch (_) {
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
      home: _body(),
    );
  }

  Widget _body() {
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
                const Text('Błąd inicjalizacji', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                const Text('Nie udało się przygotować aplikacji. Spróbuj ponownie.', textAlign: TextAlign.center),
                const SizedBox(height: 24),
                FilledButton(onPressed: _init, child: const Text('Spróbuj ponownie')),
              ],
            ),
          ),
        ),
      );
    }
    if (!_ready) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
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

class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _index = 0;

  final List<Widget> _pages = const [
    HomeScreen(),
    PlaceholderPage(title: 'Kalendarz'),
    IndexScreen(),
    PlaceholderPage(title: 'Konto'),
  ];

  static const _titles = ['Główna', 'Kalendarz', 'Indeks', 'Konto'];

  void _onTap(int i) => setState(() => _index = i);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _index == 0 || _index == 2 ? null : AppBar(title: Text(_titles[_index])),
      body: _pages[_index],
      bottomNavigationBar: MyUZBottomNavigation(
        currentIndex: _index,
        onTap: _onTap,
      ),
    );
  }
}

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