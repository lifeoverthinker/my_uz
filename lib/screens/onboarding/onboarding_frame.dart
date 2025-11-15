import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// OnboardingFrame – stały szkielet ekranu (Material Design 3)
/// Figma (mobile):
/// - SafeArea
/// - Padding: top=12, bottom=32, horizontal=24
/// Sekcje:
/// - Góra: Pomiń (TextButton, LabelMedium 12/1.33/0.5, kolor Primary)
/// - Środek: zawartość strony (child) – tylko to się zmienia/animuje
/// - Dół: kropki paginacji + Wstecz/Dalej (LabelLarge 14/1.43/0.1, radius 24)
class OnboardingFrame extends StatelessWidget {
  final int pageIndex; // 0-based
  final int totalPages; // liczba kropek
  final VoidCallback onSkip;
  final VoidCallback? onBack;
  final VoidCallback onNext;
  final bool canProceed;
  final int? hideNextArrowOnPageIndex; // jeśli ustawione, ukryje prawą strzałkę na tej stronie (0-based)
  final Widget child;

  const OnboardingFrame({
    super.key,
    required this.pageIndex,
    required this.totalPages,
    required this.onSkip,
    this.onBack,
    required this.onNext,
    required this.child,
    this.canProceed = true,
    this.hideNextArrowOnPageIndex,
  });

  @override
  Widget build(BuildContext context) {
    // Figma: Kolory i typografia (M3)
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: cs.surface,
      body: SafeArea(
        child: Column(
          children: [
            // GÓRA: Pomiń – stała pozycja
            Padding(
              // Figma: top=12, h=24
              padding: const EdgeInsets.only(top: 12, left: 24, right: 24),
              child: Align(
                alignment: Alignment.centerRight,
                child: TextButton(
                  onPressed: onSkip,
                  style: TextButton.styleFrom(
                    minimumSize: const Size(96, 40), // stała wysokość (anty-jank)
                    padding: EdgeInsets.zero,
                    tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    foregroundColor: cs.primary,
                    textStyle: AppTextStyle.myUZLabelMedium,
                  ),
                  child: const Text('Pomiń'),
                ),
              ),
            ),

            // ŚRODEK: tylko to się zmienia między screenami
            Expanded(
              child: Padding(
                // Figma: odstęp od Pomiń i od dołu sekcji
                padding: const EdgeInsets.only(top: 12, bottom: 16),
                child: child,
              ),
            ),

            // DÓŁ: kropki + nawigacja – stała pozycja
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Column(
                children: [
                  // Kropki paginacji
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(totalPages, (i) {
                      final isActive = i == pageIndex;
                      return Container(
                        margin: const EdgeInsets.symmetric(horizontal: 2.5),
                        width: 8,
                        height: 8,
                        decoration: ShapeDecoration(
                          color: isActive ? cs.primary : AppColors.myUZSysLightOutlineVariant,
                          shape: const OvalBorder(),
                        ),
                      );
                    }),
                  ),
                  const SizedBox(height: 24),

                  // Wstecz / Dalej
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      if (pageIndex > 0)
                        TextButton(
                          onPressed: onBack,
                          style: TextButton.styleFrom(
                            minimumSize: const Size(112, 40), // stała wysokość
                            backgroundColor: AppColors.myUZSysLightPrimaryFixed,
                            foregroundColor: cs.primary,
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                            textStyle: AppTextStyle.myUZLabelLarge,
                          ),
                          child: const Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(MyUz.chevron_left, size: 24),
                              SizedBox(width: 8),
                              Text('Wstecz'),
                            ],
                          ),
                        )
                      else
                        const SizedBox(width: 1, height: 1),

                      FilledButton(
                        onPressed: canProceed ? onNext : null,
                        style: FilledButton.styleFrom(
                          minimumSize: const Size(112, 40), // stała wysokość (enabled/disabled)
                          backgroundColor: cs.primary,
                          foregroundColor: cs.onPrimary,
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                          textStyle: AppTextStyle.myUZLabelLarge.copyWith(color: cs.onPrimary),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(pageIndex == totalPages - 1 ? 'Gotowe!' : 'Dalej'),
                            // Prawa strzałka tylko gdy to nie jest ostatnia strona
                            // oraz gdy bieżąca strona nie jest na liście stron, dla których chcemy ją ukryć
                            if (pageIndex != totalPages - 1 && pageIndex != hideNextArrowOnPageIndex) ...[
                              const SizedBox(width: 8),
                              const Icon(MyUz.chevron_right, size: 24),
                            ],
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),

                  // STOPKA: wersja
                  Text.rich(
                    TextSpan(
                      children: [
                        TextSpan(
                          text: 'MyUZ 2025\n',
                          style: AppTextStyle.myUZLabelMedium.copyWith(color: cs.outline),
                        ),
                        TextSpan(
                          text: 'Wersja 2025.1.0',
                          style: AppTextStyle.myUZLabelMedium.copyWith(
                            color: cs.outline,
                            fontWeight: FontWeight.w400,
                            letterSpacing: 0.4,
                          ),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 8),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}