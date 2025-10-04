import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';
import 'package:my_uz/services/classes_repository.dart';

/// Onboarding – Ekran 3 (mobile)
/// Figma: padding top=12, bottom=32, horizontal=24
/// Środek: Ilustracja (settings), Tytuły, OutlinedTextField z lupą, po wyborze podgrupy A/B
class OnboardingPage3 extends StatefulWidget {
  final VoidCallback onSkip;
  final VoidCallback onBack;
  final VoidCallback onNext;

  const OnboardingPage3({
    super.key,
    required this.onSkip,
    required this.onBack,
    required this.onNext,
  });

  @override
  State<OnboardingPage3> createState() => _OnboardingPage3State();
}

class _OnboardingPage3State extends State<OnboardingPage3> {
  final _groupCtrl = TextEditingController();
  String? _selectedGroupCode; // potwierdzony kod
  String? _subgroup; // 'A' | 'B'

  @override
  void dispose() {
    _groupCtrl.dispose();
    super.dispose();
  }

  void _confirmGroup() {
    final v = _groupCtrl.text.trim();
    if (v.isEmpty) return;
    setState(() {
      _selectedGroupCode = v;
      _subgroup = null;
    });
  }

  bool get _canProceed => _selectedGroupCode != null && _subgroup != null;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    final hasGroup = _selectedGroupCode != null;
    double illustrationHeight = 205;
    if (hasGroup) illustrationHeight = 132;
    if (bottomInset > 0) illustrationHeight = 96;

    return OnboardingFrame(
      pageIndex: 2,
      totalPages: 6,
      onSkip: widget.onSkip,
      onBack: widget.onBack,
      onNext: () async {
        // Zapisz wybraną grupę i podgrupy przed przejściem dalej
        await ClassesRepository.setGroupPrefs(_selectedGroupCode, _subgroup == null ? [] : [_subgroup!]);
        widget.onNext();
      },
      canProceed: _canProceed,
      child: ScrollConfiguration(
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
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Ilustracja – settings-rafiki (Animated height)
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
                    child: Text(
                      'Wybierz swoją grupę',
                      textAlign: TextAlign.center,
                      style: tt.headlineMedium?.copyWith(color: cs.onBackground),
                    ),
                  ),
                  const SizedBox(height: 8),
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 312),
                    child: Text(
                      'Znajdź swój plan zajęć',
                      textAlign: TextAlign.center,
                      style: AppTextStyle.myUZTitleMedium.copyWith(
                        color: cs.primary,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
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

                  // OutlinedTextField 56 (labelText + lupa w suffixIcon) – bez osobnego przycisku
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 312),
                    child: SizedBox(
                      height: 56,
                      child: TextField(
                        controller: _groupCtrl,
                        textInputAction: TextInputAction.search,
                        onSubmitted: (_) => _confirmGroup(),
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
                  const SizedBox(height: 8),

                  if (_selectedGroupCode != null)
                    ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 312),
                      child: Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          'Wybrana grupa: $_selectedGroupCode',
                          style: AppTextStyle.myUZLabelMedium.copyWith(color: cs.onSurfaceVariant),
                        ),
                      ),
                    ),
                  const SizedBox(height: 16),

                  // Panel podgrup (po wyborze grupy) – animowany
                  AnimatedSwitcher(
                    duration: const Duration(milliseconds: 280),
                    switchInCurve: Curves.easeOutCubic,
                    switchOutCurve: Curves.easeOutCubic,
                    transitionBuilder: (child, anim) {
                      final slide = Tween<Offset>(begin: const Offset(0, 0.1), end: Offset.zero).animate(anim);
                      return FadeTransition(opacity: anim, child: SlideTransition(position: slide, child: child));
                    },
                    child: _selectedGroupCode == null
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
                            _ChoicePill(label: 'A', selected: _subgroup == 'A', onTap: () => setState(() => _subgroup = 'A'), cs: cs),
                            const SizedBox(width: 16),
                            _ChoicePill(label: 'B', selected: _subgroup == 'B', onTap: () => setState(() => _subgroup = 'B'), cs: cs),
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
      ),
    );
  }
}

// --- POMOCNICZE (DRY/KISS) ---

/// Bez overscroll-glow
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}

/// Pill/Chip – radius 16
class _ChoicePill extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;
  final ColorScheme cs;

  const _ChoicePill({
    required this.label,
    required this.selected,
    required this.onTap,
    required this.cs,
  });

  @override
  Widget build(BuildContext context) {
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