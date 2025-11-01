import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';

class ScheduleAppBarAction {
  final Widget icon; // np. Icon(Icons.info_outline)
  final String? tooltip;
  final VoidCallback onTap;
  ScheduleAppBarAction({required this.icon, required this.onTap, this.tooltip});
}

class ScheduleAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final String? subtitle;
  final VoidCallback onBack;
  final List<ScheduleAppBarAction> actions;

  const ScheduleAppBar({
    super.key,
    required this.title,
    this.subtitle,
    required this.onBack,
    this.actions = const [],
  });

  @override
  Size get preferredSize => const Size.fromHeight(72);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    Widget actionSlot(ScheduleAppBarAction a) {
      return SizedBox(
        width: 48,
        height: 48,
        child: Center(
          child: InkWell(
            customBorder: const CircleBorder(),
            onTap: a.onTap,
            child: Container(
              width: 48,
              height: 48,
              decoration: const BoxDecoration(
                color: Color(0xFFF7F2F9),
                shape: BoxShape.circle,
              ),
              alignment: Alignment.center,
              child: Tooltip(
                message: a.tooltip ?? '',
                child: IconTheme(
                  data: IconThemeData(color: cs.onSurface, size: 24),
                  child: a.icon,
                ),
              ),
            ),
          ),
        ),
      );
    }

    // Wstrzykujemy odstęp 8 px między akcjami i zostawiamy ostatni najbardziej na prawo
    final List<Widget> spacedActions = [];
    for (int i = 0; i < actions.length; i++) {
      spacedActions.add(actionSlot(actions[i]));
      if (i != actions.length - 1) spacedActions.add(const SizedBox(width: 8));
    }

    return SafeArea(
      bottom: false,
      child: Container(
        height: 72,
        // Usuń lewy padding przy strzałce cofania
        padding: const EdgeInsets.only(left: 0, right: 16, top: 10, bottom: 10),
        color: Colors.white,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Back bez dodatkowego lewego paddingu
            Material(
              color: Colors.transparent,
              shape: const CircleBorder(),
              child: InkWell(
                customBorder: const CircleBorder(),
                onTap: onBack,
                child: const SizedBox(
                  width: 48,
                  height: 48,
                  child: Center(
                    child: Icon(MyUz.chevron_left, size: 24),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Container(
                alignment: Alignment.centerLeft,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: AppTextStyle.myUZTitleLarge.copyWith(color: cs.onSurface, fontSize: 18, fontWeight: FontWeight.w500)),
                    if (subtitle != null && subtitle!.isNotEmpty)
                      Text(subtitle!, style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant), maxLines: 1, overflow: TextOverflow.ellipsis),
                  ],
                ),
              ),
            ),
            Row(mainAxisSize: MainAxisSize.min, children: spacedActions),
          ],
        ),
      ),
    );
  }
}