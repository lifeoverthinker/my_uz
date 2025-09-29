import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/cards/class_card.dart';

/// Sekcja: Najbliższe zajęcia
/// Card height (Figma): 68
/// Slider height = card height (brak dodatkowego marginesu pionowego)
class UpcomingClassesSection extends StatelessWidget {
  final List<ClassModel> classes;
  final ValueChanged<ClassModel> onTap;

  const UpcomingClassesSection({
    super.key,
    required this.classes,
    required this.onTap,
  });

  static const double _kCardWidth = 264;
  static const double _kListHeight = 68; // = ClassCard

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Container(
      // tylko lewy padding sekcji, aby karty mogły wychodzić z prawej "zza krawędzi"
      padding: const EdgeInsets.fromLTRB(16, 0, 0, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _SectionHeader(
            icon: MyUz.graduation_hat_02,
            label: 'Najbliższe zajęcia',
            color: cs.onSurface,
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: _kListHeight,
            child: ListView.builder(
              padding: EdgeInsets.zero,
              scrollDirection: Axis.horizontal,
              physics: const ClampingScrollPhysics(),
              itemCount: classes.length + 1, // +1 dla trailing spacer 16
              itemBuilder: (context, i) {
                if (i == classes.length) {
                  return const SizedBox(width: 16); // końcowy padding
                }
                final c = classes[i];
                final colors = _variant(i % 3);
                final time = '${_hhmm(c.startTime)} - ${_hhmm(c.endTime)}';
                final init = _initials(c.lecturer);
                return Padding(
                  padding: EdgeInsets.only(right: i == classes.length - 1 ? 0 : 8),
                  child: SizedBox(
                    width: _kCardWidth,
                    child: InkWell(
                      borderRadius: BorderRadius.circular(8),
                      splashColor: Colors.transparent,
                      highlightColor: Colors.transparent,
                      onTap: () => onTap(c),
                      child: ClassCard(
                        title: c.subject,
                        time: time,
                        room: c.room,
                        initial: init,
                        backgroundColor: colors.bg,
                        avatarColor: colors.avatar,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  _ClassColors _variant(int v) {
    switch (v) {
      case 1:
        return const _ClassColors(
          bg: AppColors.myUZSysLightTertiaryContainer, // róż
          avatar: AppColors.myUZSysLightPrimary,
        );
      case 2:
        return const _ClassColors(
          bg: AppColors.myUZSysLightSecondaryContainer, // jasny fiolet
          avatar: AppColors.myUZSysLightPrimary,
        );
      case 0:
      default:
        return const _ClassColors(
          bg: AppColors.myUZSysLightPrimaryContainer, // fiolet
          avatar: AppColors.myUZSysLightPrimary,
        );
    }
  }

  String _hhmm(DateTime d) =>
      '${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';

  String _initials(String lecturer) {
    final parts = lecturer.trim().split(RegExp(r'\s+'));
    if (parts.isEmpty) return 'A';
    if (parts.length == 1) return parts.first.characters.first.toUpperCase();
    return (parts.first.characters.first + parts.last.characters.first).toUpperCase();
  }
}

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  const _SectionHeader({required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 20, color: color),
        const SizedBox(width: 8),
        Text(
          label,
          style: AppTextStyle.myUZTitleMedium.copyWith(
            fontSize: 18,
            height: 1.33,
            fontWeight: FontWeight.w500,
            color: color,
          ),
        ),
      ],
    );
  }
}

class _ClassColors {
  final Color bg;
  final Color avatar;
  const _ClassColors({required this.bg, required this.avatar});
}