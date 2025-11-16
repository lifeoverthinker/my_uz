// Plik: lib/screens/home/details/class_details.dart
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/widgets/sheet_scaffold.dart';
import 'package:my_uz/utils/constants.dart'; // Import globalnych stałych

abstract class ClassDetailsSheet {
  static Future<void> open(BuildContext context, ClassModel c) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black54,
      builder: (_) => _ClassDetailsDraggable(classModel: c),
    );
  }
}

const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0;
// Stała _kIconToTextGap została przeniesiona do constants.dart

class _ClassDetailsDraggable extends StatelessWidget {
  final ClassModel classModel;
  const _ClassDetailsDraggable({required this.classModel});

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0,
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _SheetScaffoldBody(
          scrollController: scrollController,
          child: _DetailsContent(classModel: classModel),
        );
      },
    );
  }
}

class _SheetScaffoldBody extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  const _SheetScaffoldBody({required this.child, required this.scrollController});

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    const handleTopGap = 8.0;
    const handleToXGap = 8.0;
    const xToHeaderGap = 12.0;

    return Container(
      margin: EdgeInsets.zero,
      padding: EdgeInsets.only(top: topPadding + handleTopGap),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
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
            const SizedBox(height: handleToXGap),
            Row(
              children: [
                AdaptiveIconSlot(
                  iconSize: 24,
                  semanticsLabel: 'Zamknij szczegóły zajęć',
                  isButton: true,
                  onTap: () => Navigator.of(context).maybePop(),
                  child: const Icon(MyUz.x_close, size: 24, color: Color(0xFF1D192B)),
                ),
                const Spacer(),
              ],
            ),
            const SizedBox(height: xToHeaderGap),
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

class _DetailsContent extends StatelessWidget {
  final ClassModel classModel;
  const _DetailsContent({required this.classModel});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final typeLabel = _mapType(classModel.type);
    final dateLine = _dateLine(classModel.startTime, classModel.endTime);

    const headerBottomGap = 28.0;
    const rowVerticalGap = 12.0;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            AdaptiveIconSlot(
              iconSize: 20, // Ustawiono na 20, aby pasowało do _TypeColorMarker
              child: _TypeColorMarker(color: AppColors.myUZSysLightPrimaryContainer),
            ),
            const SizedBox(width: kIconToTextGap), // Użycie stałej globalnej
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    classModel.subject,
                    style: AppTextStyle.myUZTitleLarge.copyWith(
                      fontWeight: FontWeight.w500,
                      color: const Color(0xFF1D192B),
                    ),
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    dateLine,
                    style: AppTextStyle.myUZBodyMedium.copyWith(
                      color: cs.onSurfaceVariant,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: headerBottomGap),
        if (typeLabel != null) ...[
          DetailRow(icon: MyUz.stand, label: typeLabel),
          const SizedBox(height: rowVerticalGap),
        ],
        DetailRow(icon: MyUz.marker_pin_04, label: classModel.room.isNotEmpty ? classModel.room : 'Sala -'),
        const SizedBox(height: 12), // Przywrócono 12 (zamiast 0) dla odstępu
        DetailRow(icon: MyUz.user_01, label: classModel.lecturer.isNotEmpty ? classModel.lecturer : 'Prowadzący -'),
      ],
    );
  }

  static String _dateLine(DateTime start, DateTime end) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final startDay = DateTime(start.year, start.month, start.day);
    final tomorrow = today.add(const Duration(days: 1));

    String dayLabel;
    if (startDay == today) {
      dayLabel = 'Dzisiaj';
    } else if (startDay == tomorrow) {
      dayLabel = 'Jutro';
    } else {
      dayLabel = _plWeekday(start.weekday);
    }

    final monthShort = _plMonthShort(start.month);
    final bool showYear = start.year != now.year;
    final datePart = showYear
        ? '$dayLabel, ${start.day} $monthShort ${start.year}'
        : '$dayLabel, ${start.day} $monthShort';

    final timePart = '${_hhmm(start)} – ${_hhmm(end)}';
    return '$datePart • $timePart';
  }

  static String _hhmm(DateTime d) => '${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
  static String _plWeekday(int weekday) { const dni = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela']; return dni[weekday - 1]; }
  static String _plMonthShort(int m) { const mies = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru']; return mies[m - 1]; }
  static String? _mapType(String? raw) {
    final r = (raw ?? '').trim();
    if (r.isEmpty) return null;
    return r;
  }
}

class _TypeColorMarker extends StatelessWidget {
  final Color color;
  const _TypeColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 20, // POPRAWKA: Rozmiar 20x20
      height: 20, // POPRAWKA: Rozmiar 20x20
      decoration: ShapeDecoration(
        color: color,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
    );
  }
}

// +++ POCZĄTEK BRAKUJĄCEGO KODU +++
// Ta klasa została przypadkowo usunięta w poprzedniej wersji

class DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  const DetailRow({super.key, required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    // const double kIconToTextGap = 12; // Używamy teraz stałej globalnej

    return Row(
      crossAxisAlignment: CrossAxisAlignment.center, // Wyrównanie do środka
      children: [
        AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: const Color(0xFF1D192B)), // POPRAWKA: Ciemny kolor ikony
        ),
        const SizedBox(width: kIconToTextGap), // Użycie stałej globalnej
        Expanded(
          child: Text(
            label,
            style: AppTextStyle.myUZBodyMedium.copyWith(
              color: cs.onSurface,
              fontWeight: FontWeight.w400,
            ),
          ),
        ),
      ],
    );
  }
}
// +++ KONIEC BRAKUJĄCEGO KODU +++