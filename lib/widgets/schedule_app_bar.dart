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
  // Zwiększamy wysokość preferowanego AppBar'a, żeby zmieścić dwie linie tekstu
  Size get preferredSize => const Size.fromHeight(72);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    Widget actionSlot(ScheduleAppBarAction a) {
      return SizedBox(
        width: 48,
        height: 48,
        child: Center(
          child: Material(
            color: Colors.transparent,
            shape: const CircleBorder(),
            child: InkWell(
              customBorder: const CircleBorder(),
              onTap: a.onTap,
              child: SizedBox(
                width: 40,
                height: 40,
                child: Center(
                  child: Tooltip(
                    message: a.tooltip ?? '',
                    preferBelow: false,
                    child: IconTheme(
                      data: IconThemeData(color: cs.onSurface, size: 24),
                      child: a.icon,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      );
    }

    // (rightPadding zostało usunięte podczas refaktoryzacji układu — teraz używamy Row/Expanded)

    return SafeArea(
      bottom: false,
      child: Container(
        height: 72,
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 10),
        color: Theme.of(context).scaffoldBackgroundColor,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Leading back button (wszystko wyrównane do środka pionowo)
            SizedBox(
              width: 56,
              child: Center(
                child: Material(
                  color: Colors.transparent,
                  shape: const CircleBorder(),
                  child: InkWell(
                    customBorder: const CircleBorder(),
                    onTap: onBack,
                    child: const SizedBox(
                      width: 40,
                      height: 40,
                      child: Center(
                        child: Icon(MyUz.chevron_left, size: 24),
                      ),
                    ),
                  ),
                ),
              ),
            ),

            // Title + subtitle w jednym Text.rich, ograniczone do 2 linii.
            Expanded(
              child: Container(
                alignment: Alignment.centerLeft,
                child: Text.rich(
                  TextSpan(
                    children: [
                      TextSpan(text: title, style: AppTextStyle.myUZTitleLarge.copyWith(color: cs.onSurface)),
                      if (subtitle != null && subtitle!.isNotEmpty) TextSpan(text: '\n${subtitle!}', style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant)),
                    ],
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ),

            // Actions row (right)
            Row(
              mainAxisSize: MainAxisSize.min,
              children: actions.map(actionSlot).toList(),
            ),
          ],
        ),
      ),
    );
  }
}
