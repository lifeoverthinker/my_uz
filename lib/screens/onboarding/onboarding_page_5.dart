import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:my_uz/theme/text_style.dart';
import 'onboarding_frame.dart';

/// Onboarding 15 (mobile)
/// Figma: padding top=12, bottom=32, horizontal=24
/// Sta2e elementy zapewnia OnboardingFrame.
/// r3dek: Ilustracja (paper-map), Tytu2y i opis.
class OnboardingPage5 extends StatelessWidget {
  final VoidCallback onSkip;
  final VoidCallback onBack;
  final VoidCallback onNext;

  const OnboardingPage5({
    super.key,
    required this.onSkip,
    required this.onBack,
    required this.onNext,
  });

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return OnboardingFrame(
      pageIndex: 4, // 0-based (to jest 5. ekran)
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
                // Ilustracja 15: paper map (Figma: h=205)
                SvgPicture.asset(
                  'assets/images/illustrations/paper-map-rafiki.svg',
                  height: 205,
                  semanticsLabel: 'Mapa kampusu 15 ilustracja',
                ),
                const SizedBox(height: 24),

                // Tytu2y i opis
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Mapa kampusu',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(color: cs.onSurface),
                  ),
                ),
                const SizedBox(height: 8),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 312),
                  child: Text(
                    'Nigdy si9 nie zagub',
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
                    'Interaktywna mapa kampusu pomoe znale budynki uczelni, bibliotek i inne wane miejsca.',
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