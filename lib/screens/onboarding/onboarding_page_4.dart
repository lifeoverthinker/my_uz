import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';

/// Onboarding – Ekran 4 (mobile)
/// Figma: padding top=12, bottom=32, horizontal=24
/// Stałe elementy (Pomiń, kropki, Wstecz/Dalej) zapewnia OnboardingFrame.
/// Środek: Ilustracja (calendar), Tytuły i opis.
class OnboardingPage4 extends StatelessWidget {
  final VoidCallback onSkip;
  final VoidCallback onBack;
  final VoidCallback onNext;

  const OnboardingPage4({
    super.key,
    required this.onSkip,
    required this.onBack,
    required this.onNext,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return OnboardingFrame(
      pageIndex: 3, // 0-based (to jest 4. ekran)
      totalPages: 6,
      onSkip: onSkip,
      onBack: onBack,
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
                // Ilustracja – calendar (Figma: h=205)
                SvgPicture.asset(
                  'assets/images/illustrations/calendar-rafiki.svg',
                  height: 205,
                  semanticsLabel: 'Terminarz i kalendarz – ilustracja',
                ),
                const SizedBox(height: 24),

                // Tytuły i opis – zgodnie z M3/typografią (Figma)
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Terminarz i kalendarz',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(color: cs.onBackground),
                  ),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Wszystkie zajęcia w jednym miejscu',
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
                    'Sprawdzaj plan zajęć, dodawaj zadania i nie przegap żadnego wydarzenia uniwersyteckiego.',
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

/// Bez overscroll-glow (DRY)
class _NoGlowScrollBehavior extends ScrollBehavior {
  const _NoGlowScrollBehavior();
  @override
  Widget buildOverscrollIndicator(BuildContext context, Widget child, ScrollableDetails details) => child;
}