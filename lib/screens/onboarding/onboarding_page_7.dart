import 'package:flutter/material.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';

/// Onboarding – Ekran 7 (Summary, bez ilustracji, styl Google/M3)
/// Figma (mobile): SafeArea; top=12; bottom=32; horizontal=24
/// Stałe elementy (Pomiń, kropki, Wstecz/Dalej) zapewnia OnboardingFrame.
/// Środek: krótki komunikat końcowy, bez ilustracji.
class OnboardingSummaryPage extends StatelessWidget {
  final VoidCallback onFinish;
  final VoidCallback onBack;

  const OnboardingSummaryPage({
    super.key,
    required this.onFinish,
    required this.onBack,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return OnboardingFrame(
      pageIndex: 5, // podświetl ostatnią (6.) kropkę
      totalPages: 6, // spójnie z resztą onboardingowych ekranów
      onSkip: onFinish,
      onBack: onBack,
      onNext: onFinish, // “Dalej” kończy onboarding
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
                // HeadlineMedium 28/1.29 – OnBackground
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Gotowe!',
                    textAlign: TextAlign.center,
                    style: tt.headlineMedium?.copyWith(color: cs.onBackground),
                  ),
                ),
                const SizedBox(height: 8),

                // TitleMedium 16/1.50/0.15, 500 – Primary
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Witaj w MyUZ',
                    textAlign: TextAlign.center,
                    style: AppTextStyle.myUZTitleMedium.copyWith(
                      color: cs.primary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // BodySmall 12/1.33/0.4 – OnSurfaceVariant
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Możesz już korzystać z aplikacji. Ustawienia personalizacji i grupy możesz zmienić później w Koncie.',
                    textAlign: TextAlign.center,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
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

/// Wyłącza systemowy overscroll glow
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}