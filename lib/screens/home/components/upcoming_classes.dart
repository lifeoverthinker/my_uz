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
  final String? groupCode;
  final List<String>? subgroups;
  final String headerTitle;
  final bool isLoading;
  final String? emptyMessage;

  const UpcomingClassesSection({
    super.key,
    required this.classes,
    required this.onTap,
    this.groupCode,
    this.subgroups,
    this.headerTitle = 'Najbliższe zajęcia',
    this.isLoading = false,
    this.emptyMessage,
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
          // Header row (title configurable)
          Row(
            children: [
              Icon(MyUz.graduation_hat_02, size: 20, color: cs.onSurface),
              const SizedBox(width: 8),
              Text(
                headerTitle,
                style: AppTextStyle.myUZTitleMedium.copyWith(
                  fontSize: 18,
                  height: 1.33,
                  fontWeight: FontWeight.w500,
                  color: cs.onSurface,
                ),
              ),
              const Spacer(),
              // NOTE: removed right-side suffix (e.g. 'jutro') per design change
            ],
          ),
          const SizedBox(height: 12),
          // Responsive horizontal list: show only as many cards as fit into available width
          SizedBox(
            height: _kListHeight,
            child: isLoading
                ? const Center(child: SizedBox(width: 28, height: 28, child: CircularProgressIndicator(strokeWidth: 3)))
                : classes.isEmpty
                    ? Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          emptyMessage ?? 'Dziś brak nadchodzących zajęć',
                          textAlign: TextAlign.left,
                          style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                        ),
                      )
                    : LayoutBuilder(builder: (context, constraints) {
                        final double maxWidth = constraints.maxWidth;
                        const double spacing = 8;
                        // compute how many cards can fit (accounting for spacing between cards)
                        final int fitCount = ((maxWidth + spacing) ~/ (_kCardWidth + spacing)).clamp(0, classes.length);
                        final visible = classes.take(fitCount).toList();
                        return ListView.builder(
                          padding: EdgeInsets.zero,
                          scrollDirection: Axis.horizontal,
                          physics: const ClampingScrollPhysics(),
                          itemCount: visible.length + 1, // trailing spacer
                          itemBuilder: (context, i) {
                            if (i == visible.length) return const SizedBox(width: 16);
                            final c = visible[i];
                            final colors = _variant(i % 3);
                            final time = '${_hhmm(c.startTime)} - ${_hhmm(c.endTime)}';
                            final abbrev = (c.type ?? '').trim();
                            final init = abbrev.isNotEmpty ? abbrev.toUpperCase() : _initials(c.lecturer);
                            return Padding(
                              padding: EdgeInsets.only(right: i == visible.length - 1 ? 0 : spacing),
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
                        );
                      }),
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


class _ClassColors {
  final Color bg;
  final Color avatar;
  const _ClassColors({required this.bg, required this.avatar});
}