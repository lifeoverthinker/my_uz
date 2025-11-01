import 'package:flutter/material.dart';
// import 'package:my_uz/icons/my_uz_icons.dart'; // <<< USUNIĘTY NIEUŻYWANY IMPORT

const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
const double _kTopRadius = 24;
const double _kHitArea = 48;
const double _kCircleSmall = 32;
const double _kCircleLarge = 40;
const double _kIconToTextGap = 12;

/// A reusable scaffold for modal bottom sheets styled like Google Calendar.
/// Provides a draggable handle, a close button, and consistent padding.
abstract class SheetScaffold {
  static Future<T?> showAsModal<T>(
      BuildContext context, {
        required Widget child,
        bool isScrollControlled = true,
        Color? barrierColor,
      }) {
    return showModalBottomSheet<T>(
      context: context,
      isScrollControlled: isScrollControlled,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: barrierColor ?? Colors.black54,
      builder: (_) => _SheetDraggable(
        initialChildSize: 1.0, // Default to full screen for modals
        child: child,
      ),
    );
  }
}

class _SheetDraggable extends StatelessWidget {
  final Widget child;
  final double initialChildSize;
  const _SheetDraggable({required this.child, required this.initialChildSize});

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: initialChildSize,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _ScaffoldBody(
          scrollController: scrollController,
          child: child,
        );
      },
    );
  }
}

class _ScaffoldBody extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  const _ScaffoldBody({required this.child, required this.scrollController});

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    const handleTopGap = 8.0;
    const handleToContentGap = 8.0;

    return Container(
      margin: EdgeInsets.zero,
      padding: EdgeInsets.only(top: topPadding + handleTopGap),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(_kTopRadius)),
        boxShadow: [
          BoxShadow(color: Color(0x4C000000), blurRadius: 3, offset: Offset(0, 1)),
          BoxShadow(color: Color(0x26000000), blurRadius: 8, offset: Offset(0, 4), spreadRadius: 3),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(horizontal, 0, horizontal, 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const GripHandle(),
            const SizedBox(height: handleToContentGap),
            Expanded(
              child: SingleChildScrollView(
                controller: scrollController,
                child: child,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// A draggable grip handle for the top of the sheet.
class GripHandle extends StatelessWidget {
  const GripHandle({super.key});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 40,
      height: 4,
      decoration: BoxDecoration(
        color: Colors.black26,
        borderRadius: BorderRadius.circular(2),
      ),
    );
  }
}

/// An icon slot with consistent sizing and hit area.
class AdaptiveIconSlot extends StatelessWidget {
  final double iconSize; // 16/20/24
  final Widget child; // icon or marker
  final VoidCallback? onTap;
  final String? semanticsLabel;
  final bool isButton;

  const AdaptiveIconSlot({
    super.key,
    required this.iconSize,
    required this.child,
    this.onTap,
    this.semanticsLabel,
    this.isButton = false,
  });

  @override
  Widget build(BuildContext context) {
    final double circle = iconSize >= 24 ? _kCircleLarge : _kCircleSmall;
    Widget inner = SizedBox(
      width: _kHitArea,
      height: _kHitArea,
      child: Align(
        alignment: Alignment.center,
        child: Container(
          width: circle,
          height: circle,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(circle / 2),
          ),
          alignment: Alignment.center,
          child: SizedBox(
            width: iconSize,
            height: iconSize,
            child: FittedBox(fit: BoxFit.contain, child: child),
          ),
        ),
      ),
    );

    if (onTap != null) {
      inner = Material(
        type: MaterialType.transparency,
        child: InkWell(
          borderRadius: BorderRadius.circular(circle / 2),
          splashColor: Colors.black12,
          onTap: onTap,
          child: inner,
        ),
      );
    }
    if (semanticsLabel != null) {
      inner = Semantics(
        button: isButton,
        label: semanticsLabel,
        child: inner,
      );
    }
    return inner;
  }
}

/// A row for displaying a piece of detail, with an icon and a label.
class DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  const DetailRow({super.key, required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: cs.onSurface),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Align(
            alignment: Alignment.centerLeft,
            child: Text(
              label,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(color: cs.onSurface),
              maxLines: 10,
              overflow: TextOverflow.visible,
            ),
          ),
        ),
      ],
    );
  }
}