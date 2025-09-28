import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';

/// OnboardingNavigator – stała góra/dół, animuje się tylko środek (MD3 expressive)
/// Figma (mobile): SafeArea, top=12, bottom=32, horizontal=24
class OnboardingNavigator extends StatefulWidget {
  final VoidCallback onFinishOnboarding;

  const OnboardingNavigator({super.key, required this.onFinishOnboarding});

  @override
  State<OnboardingNavigator> createState() => _OnboardingNavigatorState();
}

class _OnboardingNavigatorState extends State<OnboardingNavigator> {
  static const int _totalPages = 6;

  int _current = 0;
  bool _canProceed = true; // kontrola stanu "Dalej" (np. strona 3)
  bool _isForward = true; // kierunek przejścia dla animacji (MD3 shared-axis/fade-through feel)

  void _setCanProceed(bool v) {
    if (_canProceed == v) return;
    setState(() => _canProceed = v);
  }

  void _next() {
    setState(() {
      _isForward = true;
      if (_current < _totalPages - 1) {
        _current++;
        // Strona 3 (index=2) – domyślnie blokujemy "Dalej" dopóki user nie wybierze grupy i podgrupy
        _canProceed = _current == 2 ? false : true;
      } else {
        widget.onFinishOnboarding();
      }
    });
  }

  void _back() {
    if (_current == 0) return;
    setState(() {
      _isForward = false;
      _current--;
      _canProceed = _current == 2 ? false : true;
    });
  }

  void _skip() {
    widget.onFinishOnboarding();
  }

  // MD3 expressive: Fade-through + lekki slide/scale
  // - Incoming: fade in (easeOutCubic), slide z 5% szerokości osi X (kierunek zależny od _isForward), scale 0.98 -> 1.0
  // - Outgoing: fade out symetrycznie
  Widget _transitionBuilder(Widget child, Animation<double> anim) {
    final curved = CurvedAnimation(parent: anim, curve: Curves.easeOutCubic, reverseCurve: Curves.easeInCubic);

    final slide = Tween<Offset>(
      begin: Offset(_isForward ? 0.05 : -0.05, 0),
      end: Offset.zero,
    ).animate(curved);

    final scale = Tween<double>(begin: 0.98, end: 1.0).animate(curved);

    return FadeTransition(
      opacity: curved,
      child: SlideTransition(
        position: slide,
        child: ScaleTransition(
          scale: scale,
          child: child,
        ),
      ),
    );
  }

  // Mapowanie indeksu na treść strony (tylko środek)
  Widget _buildBodyFor(int index) {
    switch (index) {
      case 0:
        return const _Page1Body(); // college-students
      case 1:
        return const _Page2Body(); // hello
      case 2:
        return _Page3Body(onCanProceedChanged: _setCanProceed); // settings + autocomplete
      case 3:
        return const _Page4Body(); // calendar
      case 4:
        return const _Page5Body(); // grades
      case 5:
      default:
        return const _Page6Body(); // paper map
    }
  }

  @override
  Widget build(BuildContext context) {
    return OnboardingFrame(
      pageIndex: _current,
      totalPages: _totalPages,
      onSkip: _skip,
      onBack: _back,
      onNext: _next,
      canProceed: _canProceed,
      // ŚRODEK – animowany (MD3 expressive); góra/dół są stałe (nie animują się)
      child: AnimatedSwitcher(
        duration: const Duration(milliseconds: 320),
        switchInCurve: Curves.easeOutCubic,
        switchOutCurve: Curves.easeInCubic,
        transitionBuilder: _transitionBuilder,
        layoutBuilder: (currentChild, previousChildren) {
          // Stack zapobiega skokom wysokości, a jednocześnie pozwala animować crossfade
          return Stack(
            alignment: Alignment.topCenter,
            children: <Widget>[
              ...previousChildren,
              if (currentChild != null) currentChild,
            ],
          );
        },
        child: KeyedSubtree(
          key: ValueKey<int>(_current),
          child: _buildBodyFor(_current),
        ),
      ),
    );
  }
}

// --- WSPÓLNE: brak overscrollowego "glow"
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}

// --- PAGE 1: Landing (college-students) ---
class _Page1Body extends StatelessWidget {
  const _Page1Body();

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return ScrollConfiguration(
      behavior: const _NoGlowScrollBehavior(),
      child: SingleChildScrollView(
        physics: const ClampingScrollPhysics(),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Ilustracja – Figma: 205
              SvgPicture.asset(
                'assets/images/illustrations/college-students-rafiki.svg',
                height: 205,
                semanticsLabel: 'Ilustracja onboarding',
              ),
              const SizedBox(height: 16),

              // Tytuły
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text('Witaj w MyUZ! 👋', textAlign: TextAlign.center, style: tt.headlineMedium?.copyWith(color: cs.onBackground)),
              ),
              const SizedBox(height: 8),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text(
                  'Twój cyfrowy asystent na Uniwersytecie Zielonogórskim',
                  textAlign: TextAlign.center,
                  style: AppTextStyle.myUZTitleMedium.copyWith(color: cs.primary, fontWeight: FontWeight.w500),
                ),
              ),
              const SizedBox(height: 8),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text(
                  'Zarządzaj zajęciami, zadaniami i ocenami w jednym miejscu. Wszystko co potrzebujesz do organizacji życia studenckiego.',
                  textAlign: TextAlign.center,
                  style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                ),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }
}

// --- PAGE 2: Personalizacja (hello) ---
class _Page2Body extends StatefulWidget {
  const _Page2Body();

  @override
  State<_Page2Body> createState() => _Page2BodyState();
}

class _Page2BodyState extends State<_Page2Body> {
  int? _selected; // 0 = anon, 1 = dane
  String? _salutation; // 'Student' | 'Studentka'
  final _first = TextEditingController();
  final _last = TextEditingController();

  @override
  void dispose() {
    _first.dispose();
    _last.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Figma: Kolory i typografia (M3)
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    // Ilustracja – Figma: 205 bazowo, 132 (po wyborze), 96 (z klawiaturą)
    double illustrationHeight = 205;
    if (_selected != null) illustrationHeight = 132;
    if (bottomInset > 0) illustrationHeight = 96;

    return ScrollConfiguration(
      behavior: const _NoGlowScrollBehavior(),
      child: AnimatedPadding(
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
        padding: EdgeInsets.only(bottom: bottomInset),
        child: SingleChildScrollView(
          physics: const ClampingScrollPhysics(),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              children: [
                // Ilustracja – hello-rafiki
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: AnimatedContainer(
                    height: illustrationHeight,
                    duration: const Duration(milliseconds: 240),
                    curve: Curves.easeOutCubic,
                    child: SvgPicture.asset(
                      'assets/images/illustrations/hello-rafiki.svg',
                      fit: BoxFit.contain,
                      semanticsLabel: 'Ilustracja onboarding',
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Tytuły
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text('Jak mamy się do Ciebie zwracać?', textAlign: TextAlign.center, style: tt.headlineMedium?.copyWith(color: cs.onBackground)),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text('Personalizuj swoje doświadczenie',
                      textAlign: TextAlign.center, style: AppTextStyle.myUZTitleMedium.copyWith(color: cs.primary, fontWeight: FontWeight.w500)),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text('Wybierz tryb anonimowy lub wprowadź swoje dane osobowe',
                      textAlign: TextAlign.center, style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant)),
                ),
                const SizedBox(height: 24),

                // Karty opcji
                _OptionCardRadio(
                  title: 'Tryb anonimowy',
                  subtitle: 'Cześć, Studencie/Studentko',
                  selected: _selected == 0,
                  onTap: () => setState(() {
                    _selected = 0;
                    _salutation = null;
                    _first.clear();
                    _last.clear();
                  }),
                ),
                const SizedBox(height: 8),
                _OptionCardRadio(
                  title: 'Wprowadź dane',
                  subtitle: 'Cześć, [Twoje Imię]',
                  selected: _selected == 1,
                  onTap: () => setState(() {
                    _selected = 1;
                    _salutation = null;
                    _first.clear();
                    _last.clear();
                  }),
                ),
                const SizedBox(height: 16),

                // Panel dolny – chips lub inputy
                AnimatedSwitcher(
                  duration: const Duration(milliseconds: 280),
                  switchInCurve: Curves.easeOutCubic,
                  switchOutCurve: Curves.easeOutCubic,
                  transitionBuilder: (child, anim) {
                    final slide = Tween<Offset>(begin: const Offset(0, 0.08), end: Offset.zero).animate(anim);
                    return FadeTransition(opacity: anim, child: SlideTransition(position: slide, child: child));
                  },
                  child: _selected == 0
                      ? Column(
                    key: const ValueKey('anon'),
                    children: [
                      Text('Jak się do Ciebie zwracać?', style: AppTextStyle.myUZLabelSmall.copyWith(color: cs.primary)),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          _ChoicePill(label: 'Student', selected: _salutation == 'Student', onTap: () => setState(() => _salutation = 'Student')),
                          const SizedBox(width: 16),
                          _ChoicePill(label: 'Studentka', selected: _salutation == 'Studentka', onTap: () => setState(() => _salutation = 'Studentka')),
                        ],
                      ),
                    ],
                  )
                      : _selected == 1
                      ? Column(
                    key: const ValueKey('data'),
                    children: [
                      _input(context, hint: 'Imię'),
                      const SizedBox(height: 8),
                      _input(context, hint: 'Nazwisko'),
                    ],
                  )
                      : const SizedBox.shrink(key: ValueKey('empty')),
                ),
                const SizedBox(height: 8),
              ],
            ),
          ),
        ),
      ),
    );
  }

  /// Outlined Text Field – Figma: h=56, r=8, outline #7A757F, focus Primary
  Widget _input(BuildContext context, {required String hint}) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;
    return SizedBox(
      height: 56,
      child: TextField(
        style: tt.bodyLarge?.copyWith(color: cs.onSurface),
        textInputAction: TextInputAction.next,
        decoration: InputDecoration(
          hintText: hint,
          hintStyle: AppTextStyle.myUZBodyLarge.copyWith(color: AppColors.myUZSysLightOnSurfaceVariant),
          contentPadding: const EdgeInsets.symmetric(horizontal: 16),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: BorderSide(color: AppColors.myUZRefNeutralVariantNeutralVariant50),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: BorderSide(color: cs.primary, width: 1),
          ),
        ),
      ),
    );
  }
}

// --- PAGE 3: Wybór grupy i podgrupy (settings) + AUTOCOMPLETE ---
class _Page3Body extends StatefulWidget {
  final ValueChanged<bool> onCanProceedChanged;
  const _Page3Body({required this.onCanProceedChanged});

  @override
  State<_Page3Body> createState() => _Page3BodyState();
}

class _Page3BodyState extends State<_Page3Body> {
  final _groupCtrl = TextEditingController();
  final _groupFocus = FocusNode();

  // Prosta lista przykładowych grup (do podmiany na realne źródło)
  static const List<String> _allGroups = <String>[
    '23INF-SP',
    '23INF-NS',
    '24INF-SP',
    '24INF-NS',
    '23MAT-A',
    '23MAT-B',
    '23ELE-A',
    '23ELE-B',
  ];
  List<String> _suggestions = [];

  String? _group; // wybrany kod (potwierdzony)
  String? _sub; // 'A' | 'B'

  @override
  void initState() {
    super.initState();
    // Po wejściu na stronę 3 blokujemy "Dalej"
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.onCanProceedChanged(false);
    });
    _groupCtrl.addListener(_onTextChanged);
    _groupFocus.addListener(() => setState(() {})); // odśwież panel podpowiedzi
  }

  @override
  void dispose() {
    _groupCtrl.removeListener(_onTextChanged);
    _groupCtrl.dispose();
    _groupFocus.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    final q = _groupCtrl.text.trim();
    if (_group != null) return; // po potwierdzeniu grupy nie podpowiadamy
    if (q.length < 2) {
      if (_suggestions.isNotEmpty) setState(() => _suggestions = []);
      return;
    }
    final lower = q.toLowerCase();
    final filtered = _allGroups.where((g) => g.toLowerCase().startsWith(lower)).take(6).toList();
    setState(() => _suggestions = filtered);
  }

  void _confirmGroup([String? value]) {
    final v = (value ?? _groupCtrl.text).trim();
    if (v.isEmpty) return;
    setState(() {
      _group = v;
      _sub = null;
      _suggestions = [];
    });
    _notifyProceed();
  }

  void _selectSub(String s) {
    setState(() => _sub = s);
    _notifyProceed();
  }

  void _notifyProceed() {
    final ok = (_group != null && _sub != null);
    widget.onCanProceedChanged(ok);
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    final panelVisible = _group != null;
    final showAutocomplete = _groupFocus.hasFocus && _group == null && _suggestions.isNotEmpty;

    double illustrationHeight = 205;
    if (panelVisible || showAutocomplete) illustrationHeight = 132;
    if (bottomInset > 0) illustrationHeight = 96;

    return ScrollConfiguration(
      behavior: const _NoGlowScrollBehavior(),
      child: AnimatedPadding(
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
        padding: EdgeInsets.only(bottom: bottomInset),
        child: SingleChildScrollView(
          physics: const ClampingScrollPhysics(),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              children: [
                // Ilustracja – settings
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: AnimatedContainer(
                    height: illustrationHeight,
                    duration: const Duration(milliseconds: 240),
                    curve: Curves.easeOutCubic,
                    child: SvgPicture.asset(
                      'assets/images/illustrations/settings-rafiki.svg',
                      fit: BoxFit.contain,
                      semanticsLabel: 'Ustawienia – ilustracja onboarding',
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Tytuły
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text('Wybierz swoją grupę', textAlign: TextAlign.center, style: tt.headlineMedium?.copyWith(color: cs.onBackground)),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text('Znajdź swój plan zajęć',
                      textAlign: TextAlign.center, style: AppTextStyle.myUZTitleMedium.copyWith(color: cs.primary, fontWeight: FontWeight.w500)),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Wybierz swoją grupę i podgrupę, aby dostosować aplikację do Twojego rozkładu zajęć',
                    textAlign: TextAlign.center,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                  ),
                ),
                const SizedBox(height: 24),

                // Label sekcji
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Align(
                    alignment: Alignment.centerLeft,
                    child: Text('Wybierz swoją grupę:', style: AppTextStyle.myUZLabelSmall.copyWith(color: cs.primary)),
                  ),
                ),
                const SizedBox(height: 8),

                // Pole: OutlinedTextField 56 z labelText + suffix (lupa) + onChanged (autocomplete)
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: SizedBox(
                    height: 56,
                    child: TextField(
                      controller: _groupCtrl,
                      focusNode: _groupFocus,
                      textInputAction: TextInputAction.search,
                      onSubmitted: (_) => _confirmGroup(),
                      onChanged: (_) => _onTextChanged(),
                      style: tt.bodyLarge?.copyWith(color: cs.onSurface),
                      decoration: InputDecoration(
                        labelText: 'Kod grupy',
                        labelStyle: AppTextStyle.myUZBodySmall.copyWith(color: AppColors.myUZSysLightOnSurfaceVariant),
                        hintText: 'np. 23INF-SP',
                        hintStyle: AppTextStyle.myUZBodyLarge.copyWith(color: AppColors.myUZSysLightOnSurfaceVariant),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: AppColors.myUZRefNeutralVariantNeutralVariant50, width: 1),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: cs.primary, width: 1),
                        ),
                        suffixIcon: IconButton(
                          onPressed: _confirmGroup,
                          icon: Icon(MyUz.search_sm, color: cs.primary),
                          splashColor: Colors.transparent,
                          highlightColor: Colors.transparent,
                        ),
                      ),
                    ),
                  ),
                ),

                // Autocomplete dropdown (MD3 surface, max ~5 pozycji)
                AnimatedSwitcher(
                  duration: const Duration(milliseconds: 200),
                  switchInCurve: Curves.easeOutCubic,
                  switchOutCurve: Curves.easeInCubic,
                  transitionBuilder: (child, anim) {
                    final slide = Tween<Offset>(begin: const Offset(0, -0.05), end: Offset.zero).animate(anim);
                    return FadeTransition(opacity: anim, child: SlideTransition(position: slide, child: child));
                  },
                  child: showAutocomplete
                      ? ConstrainedBox(
                    key: const ValueKey('autocomplete'),
                    constraints: const BoxConstraints(maxWidth: 312, maxHeight: 200),
                    child: Container(
                      margin: const EdgeInsets.only(top: 8),
                      decoration: ShapeDecoration(
                        color: Theme.of(context).colorScheme.surface,
                        shape: RoundedRectangleBorder(
                          side: BorderSide(color: Theme.of(context).colorScheme.outlineVariant),
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: ListView.separated(
                        padding: const EdgeInsets.symmetric(vertical: 4),
                        shrinkWrap: true,
                        itemCount: _suggestions.length,
                        separatorBuilder: (_, __) => Divider(height: 1, color: Theme.of(context).colorScheme.outlineVariant),
                        itemBuilder: (context, i) {
                          final item = _suggestions[i];
                          return InkWell(
                            onTap: () => _confirmGroup(item),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                              child: Row(
                                children: [
                                  Icon(MyUz.search_sm, color: Theme.of(context).colorScheme.primary, size: 18),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      item,
                                      style: AppTextStyle.myUZBodyMedium.copyWith(
                                        color: Theme.of(context).colorScheme.onSurface,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                  )
                      : const SizedBox.shrink(key: ValueKey('autocomplete_empty')),
                ),
                const SizedBox(height: 8),

                if (_group != null)
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 312),
                    child: Align(
                      alignment: Alignment.centerLeft,
                      child: Text('Wybrana grupa: $_group', style: AppTextStyle.myUZLabelMedium.copyWith(color: cs.onSurfaceVariant)),
                    ),
                  ),
                const SizedBox(height: 16),

                // Panel podgrup – pojawia się po wyborze grupy
                AnimatedSwitcher(
                  duration: const Duration(milliseconds: 280),
                  switchInCurve: Curves.easeOutCubic,
                  switchOutCurve: Curves.easeOutCubic,
                  transitionBuilder: (child, anim) {
                    final slide = Tween<Offset>(begin: const Offset(0, 0.08), end: Offset.zero).animate(anim);
                    return FadeTransition(opacity: anim, child: SlideTransition(position: slide, child: child));
                  },
                  child: _group == null
                      ? const SizedBox.shrink(key: ValueKey('panel_empty'))
                      : Column(
                    key: const ValueKey('panel_subgroups'),
                    children: [
                      ConstrainedBox(
                        constraints: const BoxConstraints(maxWidth: 312),
                        child: Align(
                          alignment: Alignment.centerLeft,
                          child: Text('Wybierz swoją podgrupę:', style: AppTextStyle.myUZLabelSmall.copyWith(color: cs.primary)),
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          _ChoicePill(label: 'A', selected: _sub == 'A', onTap: () => _selectSub('A')),
                          const SizedBox(width: 16),
                          _ChoicePill(label: 'B', selected: _sub == 'B', onTap: () => _selectSub('B')),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

// --- PAGE 4–6: proste strony z ilustracją i tekstami (calendar / grades / paper map) ---

class _Page4Body extends StatelessWidget {
  const _Page4Body();

  @override
  Widget build(BuildContext context) {
    return const _SimplePageBody(
      asset: 'assets/images/illustrations/calendar-rafiki.svg',
      title: 'Terminarz i kalendarz',
      subtitle: 'Wszystkie zajęcia w jednym miejscu',
      body: 'Sprawdzaj plan zajęć, dodawaj zadania i nie przegap żadnego wydarzenia uniwersyteckiego.',
    );
  }
}

class _Page5Body extends StatelessWidget {
  const _Page5Body();

  @override
  Widget build(BuildContext context) {
    return const _SimplePageBody(
      asset: 'assets/images/illustrations/grades-rafiki.svg',
      title: 'Osobisty indeks',
      subtitle: 'Śledź swoje postępy lokalnie',
      body: 'Dodawaj własne oceny, plusy z aktywności i śledź postępy – wszystko lokalnie na urządzeniu.',
    );
  }
}

class _Page6Body extends StatelessWidget {
  const _Page6Body();

  @override
  Widget build(BuildContext context) {
    return const _SimplePageBody(
      asset: 'assets/images/illustrations/paper-map-rafiki.svg',
      title: 'Mapa kampusu',
      subtitle: 'Nigdy się nie zagub',
      body: 'Interaktywna mapa kampusu pomoże znaleźć budynki uczelni, bibliotekę i inne ważne miejsca.',
    );
  }
}

/// Prosty szablon stron 4–6
class _SimplePageBody extends StatelessWidget {
  final String asset;
  final String title;
  final String subtitle;
  final String body;

  const _SimplePageBody({
    required this.asset,
    required this.title,
    required this.subtitle,
    required this.body,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return ScrollConfiguration(
      behavior: const _NoGlowScrollBehavior(),
      child: SingleChildScrollView(
        physics: const ClampingScrollPhysics(),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            children: [
              SvgPicture.asset(asset, height: 205),
              const SizedBox(height: 24),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text(title, textAlign: TextAlign.center, style: AppTextStyle.myUZHeadlineMedium.copyWith(color: cs.onBackground)),
              ),
              const SizedBox(height: 8),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text(subtitle, textAlign: TextAlign.center, style: AppTextStyle.myUZTitleMedium.copyWith(color: cs.primary, fontWeight: FontWeight.w500)),
              ),
              const SizedBox(height: 8),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 312),
                child: Text(body, textAlign: TextAlign.center, style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant)),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }
}

// --- POMOCNICZE (DRY/KISS) ---

/// Karta z radiem – minHeight 56, vPad=12 hPad=16, radius 8, border 1
class _OptionCardRadio extends StatelessWidget {
  final String title;
  final String subtitle;
  final bool selected;
  final VoidCallback onTap;

  const _OptionCardRadio({
    required this.title,
    required this.subtitle,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      splashFactory: NoSplash.splashFactory,
      overlayColor: MaterialStateProperty.all(Colors.transparent),
      child: ConstrainedBox(
        constraints: const BoxConstraints(minHeight: 56),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: ShapeDecoration(
            color: selected ? cs.secondaryContainer : Colors.transparent,
            shape: RoundedRectangleBorder(
              side: BorderSide(width: 1, color: selected ? cs.primary : AppColors.myUZSysLightOutlineVariant),
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: Row(
            children: [
              // Radiowy “bullet” – CircleBorder(side: …)
              Container(
                width: 20,
                height: 20,
                decoration: ShapeDecoration(
                  color: Colors.transparent,
                  shape: CircleBorder(
                    side: BorderSide(
                      color: selected ? cs.primary : AppColors.myUZSysLightOutlineVariant,
                      width: 2,
                    ),
                  ),
                ),
                child: Center(
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    curve: Curves.easeOutCubic,
                    width: 10,
                    height: 10,
                    decoration: ShapeDecoration(
                      color: selected ? cs.primary : Colors.transparent,
                      shape: const CircleBorder(),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),

              // Teksty (elastycznie, bez overflow)
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: AppTextStyle.myUZLabelMedium.copyWith(color: cs.onSurfaceVariant), maxLines: 1, overflow: TextOverflow.ellipsis),
                    const SizedBox(height: 2),
                    Text(subtitle, style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant), maxLines: 2, overflow: TextOverflow.ellipsis),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Pill/Chip selekcji – radius 16
class _ChoicePill extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;

  const _ChoicePill({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Material(
      color: selected ? cs.primary : Colors.white,
      shape: RoundedRectangleBorder(
        side: BorderSide(width: selected ? 0 : 1, color: selected ? Colors.transparent : cs.primary),
        borderRadius: BorderRadius.circular(16),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        splashFactory: NoSplash.splashFactory,
        overlayColor: MaterialStateProperty.all(Colors.transparent),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Text(
            label,
            style: AppTextStyle.myUZLabelMedium.copyWith(
              color: selected ? cs.onPrimary : AppColors.myUZSysLightOnPrimaryFixedVariant,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ),
    );
  }
}