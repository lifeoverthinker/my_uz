import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/task_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/widgets/cards/task_card.dart';

/// Sekcja: Zadania
/// Card height (Figma): 84
/// Slider height = 84 (dokładnie wysokość karty)
class TasksSection extends StatelessWidget {
  final List<TaskModel> tasks;
  final ValueChanged<TaskModel> onTap;

  const TasksSection({
    super.key,
    required this.tasks,
    required this.onTap,
  });

  static const double _kCardWidth = 264;
  static const double _kListHeight = 84; // = TaskCard

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.fromLTRB(16, 0, 0, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _SectionHeader(
            icon: MyUz.book_open_01,
            label: 'Zadania',
            color: cs.onSurface,
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: _kListHeight,
            child: ListView.builder(
              padding: EdgeInsets.zero,
              scrollDirection: Axis.horizontal,
              physics: const ClampingScrollPhysics(),
              itemCount: tasks.length + 1, // +1 trailing spacer
              itemBuilder: (context, i) {
                if (i == tasks.length) return const SizedBox(width: 16);
                final t = tasks[i];
                final colors = _variant(i % 2);
                final desc = 'Do: ${_ddmm(t.deadline)} • ${t.subject}';
                final initials = _subjectInitials(t.subject);
                return Padding(
                  padding: EdgeInsets.only(right: i == tasks.length - 1 ? 0 : 8),
                  child: SizedBox(
                    width: _kCardWidth,
                    child: InkWell(
                      borderRadius: BorderRadius.circular(8),
                      splashColor: Colors.transparent,
                      highlightColor: Colors.transparent,
                      onTap: () => onTap(t),
                      child: TaskCard(
                        title: t.title,
                        description: desc,
                        initial: initials,
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

  _TaskColors _variant(int v) {
    switch (v) {
      case 1:
        return const _TaskColors(
          bg: AppColors.myUZSysLightPrimaryContainer,   // fiolet
          avatar: AppColors.myUZSysLightTertiary,
        );
      case 0:
      default:
        return const _TaskColors(
          bg: AppColors.myUZSysLightTertiaryContainer,  // róż
          avatar: AppColors.myUZSysLightTertiary,
        );
    }
  }

  String _ddmm(DateTime d) =>
      '${d.day.toString().padLeft(2, '0')}.${d.month.toString().padLeft(2, '0')}';

  String _subjectInitials(String s) => ClassesRepository.initialsFromName(s);
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

class _TaskColors {
  final Color bg;
  final Color avatar;
  const _TaskColors({required this.bg, required this.avatar});
}