// Plik: lib/widgets/sheet_scaffold.dart
import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/utils/constants.dart'; // <-- DODANO IMPORT

/// Reużywalny "szkielet" dla modali wysuwanych od dołu.
/// Zapewnia spójny uchwyt (GripHandle), tło i cień.
abstract class SheetScaffold {
  static Future<T?> showAsModal<T>(
      BuildContext context, {
        required Widget child,
        bool isScrollControlled = true,
        bool useSafeArea = true,
        Color backgroundColor = Colors.white,
        Color barrierColor = Colors.black54,
        double radius = 24.0,
      }) {
    return showModalBottomSheet<T>(
      context: context,
      isScrollControlled: isScrollControlled,
      useSafeArea: useSafeArea,
      backgroundColor: Colors.transparent, // Tło zapewnia Catcher
      barrierColor: barrierColor,
      builder: (_) => _SheetScaffoldCatcher(
        radius: radius,
        color: backgroundColor,
        child: child,
      ),
    );
  }
}

/// Widżet "łapiący" tło, dodający uchwyt i padding dla paska statusu.
/// Używany przez DraggableScrollableSheet w innych widokach (np. ClassDetails).
class _SheetScaffoldCatcher extends StatelessWidget {
  final Widget child;
  final double radius;
  final Color color;
  const _SheetScaffoldCatcher({
    required this.child,
    required this.radius,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    return Container(
      margin: EdgeInsets.zero,
      padding: EdgeInsets.only(top: topPadding + 8.0),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.vertical(top: Radius.circular(radius)),
        boxShadow: const [
          BoxShadow(color: Color(0x4C000000), blurRadius: 3, offset: Offset(0, 1)),
          BoxShadow(
              color: Color(0x26000000),
              blurRadius: 8,
              offset: Offset(0, 4),
              spreadRadius: 3),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const GripHandle(),
          Flexible(
            child: child,
          ),
        ],
      ),
    );
  }
}

/// Szary uchwyt (grip handle) na górze modala.
class GripHandle extends StatelessWidget {
  const GripHandle({super.key});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 32,
      height: 4,
      decoration: ShapeDecoration(
        color: AppColors.myUZSysLightOutlineVariant,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(100),
        ),
      ),
    );
  }
}

/// "Slot" na ikonę, zapewniający stałą szerokość (domyślnie 40px)
/// dla spójnego wyrównania list (jak w Google Calendar).
class AdaptiveIconSlot extends StatelessWidget {
  final Widget child;
  final double iconSize;
  final bool isButton;
  final String? semanticsLabel;
  final VoidCallback? onTap;

  const AdaptiveIconSlot({
    super.key,
    required this.child,
    required this.iconSize,
    this.isButton = false,
    this.semanticsLabel,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final content = Center(
      child: SizedBox(
        width: iconSize,
        height: iconSize,
        child: child,
      ),
    );

    // POPRAWKA: Użycie stałej globalnej kIconSlotWidth
    final box = SizedBox(
      width: kIconSlotWidth,
      height: (iconSize + 8).clamp(kIconSlotWidth, double.infinity),
      child: content,
    );

    if (isButton && onTap != null) {
      return InkWell(
        onTap: onTap,
        customBorder: const CircleBorder(),
        child: Semantics(
          label: semanticsLabel,
          button: true,
          child: box,
        ),
      );
    }
    return Semantics(
      label: semanticsLabel,
      child: box,
    );
  }
}