import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'onboarding_frame.dart';

/// Onboarding – Ekran 1 (mobile)
/// Figma: SafeArea, padding top=12, bottom=32, horizontal=24
/// Sekcje (stałe w OnboardingFrame):
/// - Góra: Pomiń (LabelMedium 12/1.33/0.5, Primary)
/// - Środek: Ilustracja + Tytuły (to się zmienia/animuje)
/// - Dół: Kropki + Wstecz/Dalej (LabelLarge 14/1.43/0.1, radius 24)
class LandingPage extends StatelessWidget {
  final VoidCallback onSkip;
  final VoidCallback onNext;

  const LandingPage({Key? key, required this.onSkip, required this.onNext}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return OnboardingFrame(
      pageIndex: 0,
      totalPages: 6,
      onSkip: onSkip,
      onBack: null, // brak Wstecz na 1-szym ekranie
      onNext: onNext,
      canProceed: true,
      child: ScrollConfiguration(
        behavior: const _NoGlowScrollBehavior(),
        child: SingleChildScrollView(
          physics: const ClampingScrollPhysics(),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Ilustracja (Figma): college-students – height 205
                SvgPicture.asset(
                  'assets/images/illustrations/college-students-rafiki.svg',
                  height: 205,
                  semanticsLabel: 'Ilustracja onboarding',
                ),
                const SizedBox(height: 16),

                // HeadlineMedium 28/1.29 – OnBackground
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Witaj w MyUZ! 👋',
                    textAlign: TextAlign.center,
                    style: tt.headlineMedium?.copyWith(color: cs.onBackground),
                  ),
                ),
                const SizedBox(height: 8),

                // TitleMedium 16/1.50/0.15, 500 – Primary
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Twój cyfrowy asystent na Uniwersytecie Zielonogórskim',
                    textAlign: TextAlign.center,
                    style: AppTextStyle.myUZTitleMedium.copyWith(
                      color: cs.primary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                const SizedBox(height: 8),

                // BodySmall 12/1.33/0.4 – OnSurfaceVariant
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Zarządzaj zajęciami, zadaniami i ocenami w jednym miejscu. Wszystko co potrzebujesz do organizacji życia studenckiego.',
                    textAlign: TextAlign.center,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                  ),
                ),
                const SizedBox(height: 16),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Wyłącza systemowy overscroll-glow (DRY)
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}