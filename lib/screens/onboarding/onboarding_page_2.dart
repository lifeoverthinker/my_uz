import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';

/// Onboarding – Ekran 2 (mobile)
/// Figma: padding top=12, bottom=32, horizontal=24
/// Stałe: Pomiń + kropki + Wstecz/Dalej w OnboardingFrame
class OnboardingPage2 extends StatefulWidget {
  final VoidCallback onSkip;
  final VoidCallback onBack;
  final VoidCallback onNext;

  const OnboardingPage2({
    super.key,
    required this.onSkip,
    required this.onBack,
    required this.onNext,
  });

  @override
  State<OnboardingPage2> createState() => _OnboardingPage2State();
}

class _OnboardingPage2State extends State<OnboardingPage2> {
  int? _selectedOption; // 0 = anonim, 1 = dane, null = nic
  String? _salutation; // 'Student' | 'Studentka'
  final _firstNameCtrl = TextEditingController();
  final _lastNameCtrl = TextEditingController();
  final _firstNameFocus = FocusNode();
  final _lastNameFocus = FocusNode();

  @override
  void dispose() {
    _firstNameCtrl.dispose();
    _lastNameCtrl.dispose();
    _firstNameFocus.dispose();
    _lastNameFocus.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    // Ilustracja: 205 (bazowo) -> 132 (po wyborze opcji) -> 96 (z klawiaturą)
    double illustrationHeight = 205;
    if (_selectedOption != null) illustrationHeight = 132;
    if (bottomInset > 0) illustrationHeight = 96;

    return OnboardingFrame(
      pageIndex: 1,
      totalPages: 6,
      onSkip: widget.onSkip,
      onBack: widget.onBack,
      onNext: widget.onNext,
      canProceed: true, // zawsze można iść dalej
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
                  // Ilustracja – hello-rafiki (Animated height)
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

                  // Tytuły – Headline / Title / Body (maxWidth=312)
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 312),
                    child: Text(
                      'Jak mamy się do Ciebie zwracać?',
                      textAlign: TextAlign.center,
                      style: tt.headlineMedium?.copyWith(color: cs.onBackground),
                    ),
                  ),
                  const SizedBox(height: 8),
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 312),
                    child: Text(
                      'Personalizuj swoje doświadczenie',
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
                      'Wybierz tryb anonimowy lub wprowadź swoje dane osobowe',
                      textAlign: TextAlign.center,
                      style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Karty wyboru – minHeight=56, vPad=12, hPad=16
                  _OptionCardRadio(
                    value: 0,
                    groupValue: _selectedOption,
                    title: 'Tryb anonimowy',
                    subtitle: 'Cześć, Studencie/Studentko',
                    cs: cs,
                    onChanged: (v) => setState(() {
                      _selectedOption = v;
                      _salutation = null;
                      _firstNameCtrl.clear();
                      _lastNameCtrl.clear();
                    }),
                  ),
                  const SizedBox(height: 8),
                  _OptionCardRadio(
                    value: 1,
                    groupValue: _selectedOption,
                    title: 'Wprowadź dane',
                    subtitle: 'Cześć, [Twoje Imię]',
                    cs: cs,
                    onChanged: (v) => setState(() {
                      _selectedOption = v;
                      _salutation = null;
                      _firstNameCtrl.clear();
                      _lastNameCtrl.clear();
                    }),
                  ),
                  const SizedBox(height: 16),

                  // Panel dolny – chips (anonim) lub inputy (dane)
                  AnimatedSwitcher(
                    duration: const Duration(milliseconds: 280),
                    switchInCurve: Curves.easeOutCubic,
                    switchOutCurve: Curves.easeOutCubic,
                    transitionBuilder: (child, anim) {
                      final slide = Tween<Offset>(begin: const Offset(0, 0.1), end: Offset.zero).animate(anim);
                      return FadeTransition(opacity: anim, child: SlideTransition(position: slide, child: child));
                    },
                    child: _buildBottomPanel(cs, tt),
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

  // --- PANEL DOLNY (animowany) ---
  Widget _buildBottomPanel(ColorScheme cs, TextTheme tt) {
    if (_selectedOption == 0) {
      // Anonimowy -> chips
      return Column(
        key: const ValueKey('panel_anon'),
        children: [
          Text('Jak się do Ciebie zwracać?', style: AppTextStyle.myUZLabelSmall.copyWith(color: cs.primary)),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _ChoicePill(label: 'Student', selected: _salutation == 'Student', onTap: () {
                setState(() => _salutation = 'Student');
              }, cs: cs),
              const SizedBox(width: 16),
              _ChoicePill(label: 'Studentka', selected: _salutation == 'Studentka', onTap: () {
                setState(() => _salutation = 'Studentka');
              }, cs: cs),
            ],
          ),
        ],
      );
    } else if (_selectedOption == 1) {
      // Wprowadź dane -> inputy
      return Column(
        key: const ValueKey('panel_input'),
        children: [
          _OutlinedTextField(
            controller: _firstNameCtrl,
            hint: 'Imię',
            cs: cs,
            tt: tt,
            focusNode: _firstNameFocus,
            textInputAction: TextInputAction.next,
            onSubmitted: (_) => _lastNameFocus.requestFocus(),
          ),
          const SizedBox(height: 8),
          _OutlinedTextField(
            controller: _lastNameCtrl,
            hint: 'Nazwisko',
            cs: cs,
            tt: tt,
            focusNode: _lastNameFocus,
            textInputAction: TextInputAction.done,
            onSubmitted: (_) => widget.onNext(),
          ),
        ],
      );
    }
    return const SizedBox.shrink(key: ValueKey('panel_empty'));
  }
}

// --- POMOCNICZE (DRY/KISS) ---

/// Bez overscroll-glow
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}

/// Karta z radiem – minHeight 56, vPad=12 hPad=16, radius 8, border 1
class _OptionCardRadio extends StatelessWidget {
  final int value;
  final int? groupValue;
  final String title;
  final String subtitle;
  final ColorScheme cs;
  final ValueChanged<int> onChanged;

  const _OptionCardRadio({
    required this.value,
    required this.groupValue,
    required this.title,
    required this.subtitle,
    required this.cs,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final selected = value == groupValue;

    return InkWell(
      onTap: () => onChanged(value),
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
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Radio – bez overlay
              RadioTheme(
                data: RadioTheme.of(context).copyWith(
                  fillColor: MaterialStateProperty.all(cs.primary),
                  overlayColor: MaterialStateProperty.all(Colors.transparent),
                  materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                  visualDensity: VisualDensity.compact,
                ),
                child: Radio<int>(
                  value: value,
                  groupValue: groupValue,
                  onChanged: (_) => onChanged(value),
                ),
              ),
              const SizedBox(width: 12),
              // Teksty
              Expanded(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
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
        onTap: onTap,
        splashFactory: NoSplash.splashFactory,
        overlayColor: MaterialStateProperty.all(Colors.transparent),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Text(
            label,
            style: AppTextStyle.myUZLabelMedium.copyWith(
              color: selected ? cs.onPrimary : AppColors.myUZSysLightOnPrimaryFixedVariant, // #4F3D74
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ),
    );
  }
}

/// Outlined Text Field – h=56, hPad=16, outline #7A757F, focus Primary
class _OutlinedTextField extends StatelessWidget {
  final TextEditingController controller;
  final String hint;
  final ColorScheme cs;
  final TextTheme tt;
  final FocusNode? focusNode;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onSubmitted;

  const _OutlinedTextField({
    required this.controller,
    required this.hint,
    required this.cs,
    required this.tt,
    this.focusNode,
    this.textInputAction,
    this.onSubmitted,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 56,
      child: TextField(
        controller: controller,
        focusNode: focusNode,
        style: tt.bodyLarge?.copyWith(color: cs.onSurface),
        textInputAction: textInputAction ?? TextInputAction.next,
        onSubmitted: onSubmitted,
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
