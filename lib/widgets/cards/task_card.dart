import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

/// TaskCard – wygląd jak w makiecie + kompaktowy layout bez zbędnych odstępów.
/// - szerokość 264 zapewnia rodzic
/// - tło: #E8DEF8 (możesz nadpisać przez backgroundColor)
/// - tytuł (Inter 14 w500 #222)
/// - meta (ikona kalendarza + data „dzień mies.”)
/// - druga linia: subject (albo type gdy subject pusty)
/// - opcjonalny awatar 32x32 z inicjałem po prawej
class TaskCard extends StatelessWidget {
  final String title;
  final DateTime deadline;
  final String subject;
  final String? type;
  final VoidCallback? onTap;
  final bool showAvatar;
  final Color? backgroundColor;

  const TaskCard({
    super.key,
    required this.title,
    required this.deadline,
    required this.subject,
    this.type,
    this.onTap,
    this.showAvatar = true,
    this.backgroundColor,
  });

  static const _kPad = 12.0;
  static const _kGap = 16.0;
  static const _kAvatarSize = 32.0;

  static const _titleStyle = TextStyle(
    color: Color(0xFF222222),
    fontSize: 14,
    fontFamily: 'Inter',
    fontWeight: FontWeight.w500,
    height: 1.43,
    letterSpacing: 0.10,
  );

  static const _metaStyle = TextStyle(
    color: Color(0xFF494949),
    fontSize: 12,
    fontFamily: 'Inter',
    fontWeight: FontWeight.w400,
    height: 1.33,
    letterSpacing: 0.40,
  );

  static const _subStyle = TextStyle(
    color: Color(0xFF494949),
    fontSize: 12,
    fontFamily: 'Inter',
    fontWeight: FontWeight.w400,
    height: 1.33,
    letterSpacing: 0.40,
  );

  String get _secondaryText {
    final s = subject.trim();
    if (s.isNotEmpty) return s;
    final t = (type ?? '').trim();
    if (t.isNotEmpty) return t;
    return '';
  }

  String get _initial {
    final base = subject.trim().isNotEmpty ? subject : title;
    if (base.trim().isEmpty) return '?';
    final r = base.trim().characters.first;
    return r.toUpperCase();
  }

  String _dayMonth(DateTime d) {
    final day = DateFormat('d', 'pl').format(d);
    final mon = DateFormat('LLL', 'pl').format(d);
    return '$day $mon';
  }

  @override
  Widget build(BuildContext context) {
    final hasAvatar = showAvatar;
    final textWidth = hasAvatar ? 192.0 : 240.0;

    final textColumn = SizedBox(
      width: textWidth,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: _titleStyle, maxLines: 1, overflow: TextOverflow.ellipsis),
          const SizedBox(height: 4),
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(MyUz.calendar, size: 16, color: Color(0xFF494949)),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  _dayMonth(deadline),
                  style: _metaStyle,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          if (_secondaryText.isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(_secondaryText, style: _subStyle, maxLines: 1, overflow: TextOverflow.ellipsis),
          ],
        ],
      ),
    );

    final avatar = hasAvatar
        ? SizedBox(
      width: _kAvatarSize,
      height: _kAvatarSize,
      child: Stack(
        children: [
          Positioned(
            left: 0,
            top: 0,
            child: Container(
              width: _kAvatarSize,
              height: _kAvatarSize,
              decoration: const ShapeDecoration(
                color: Color(0xFF7D5260),
                shape: OvalBorder(),
              ),
            ),
          ),
          Positioned(
            left: 0,
            top: 0,
            child: SizedBox(
              width: _kAvatarSize,
              height: _kAvatarSize,
              child: Center(
                child: Text(
                  _initial,
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    color: Color(0xFFFFFBFE),
                    fontSize: 16,
                    fontFamily: 'Roboto',
                    fontWeight: FontWeight.w500,
                    height: 1.50,
                    letterSpacing: 0.15,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    )
        : const SizedBox.shrink();

    final content = Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Flexible(child: textColumn),
        if (hasAvatar) ...[
          const SizedBox(width: _kGap),
          avatar,
        ],
      ],
    );

    final card = Container(
      padding: const EdgeInsets.all(_kPad),
      decoration: ShapeDecoration(
        color: backgroundColor ?? const Color(0xFFE8DEF8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: content,
    );

    if (onTap == null) return card;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: card,
      ),
    );
  }
}