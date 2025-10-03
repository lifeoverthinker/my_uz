import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'package:my_uz/theme/text_style.dart';

/// Arkusz szczegółów zajęć – wariant “Google Calendar style”
/// RÓŻNICE vs poprzednia wersja:
/// - NIE zajmuje całego ekranu przy starcie (initialChildSize ~55% wysokości – można przeciągnąć wyżej).
/// - DraggableScrollableSheet (expand:false) wewnątrz showModalBottomSheet.
/// - Brak sztucznego wymuszania wysokości -> koniec z overflow / żółtymi liniami.
/// - Możliwość dalszego przeciągnięcia do maxChildSize (95% ekranu) jak w Calendar.
/// - Natywna animacja wejścia/wyjścia (slide + fade barrier) Material3.
///
/// TODO (opcjonalnie później):
/// - Dynamiczne dostosowanie initialChildSize do realnej wysokości treści (pomiar po build).
/// - Akcje (Edytuj / Usuń) w dolnym sticky obszarze.
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

// KONFIG (fraction wysokości ekranu)
const double _kMinChildFraction = 0.40;
const double _kMaxChildFraction = 1.0; // Zmieniono z 0.95 na 1.0
const double _kTopRadius = 24;
const double _kHitArea = 48; // hit area dla ikon/markera ala Material (min 48x48)
const double _kCircleSmall = 32; // dla ikon 16/20
const double _kCircleLarge = 40; // dla ikon 24
const double _kIconToTextGap = 12; // odstęp ikona -> tekst

/// Kontener z DraggableScrollableSheet (peek -> expand)
class _ClassDetailsDraggable extends StatelessWidget {
  final ClassModel classModel;
  const _ClassDetailsDraggable({required this.classModel});

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      minChildSize: _kMinChildFraction,
      initialChildSize: 1.0, // Zmieniono na pełną wysokość od razu
      maxChildSize: _kMaxChildFraction,
      builder: (context, scrollController) {
        return _SheetScaffold(
          scrollController: scrollController,
          child: _DetailsContent(classModel: classModel),
        );
      },
    );
  }
}

/// Szkielet arkusza: tło + uchwyt + przycisk X + przewijalna treść
class _SheetScaffold extends StatelessWidget {
  final Widget child;
  final ScrollController scrollController;
  const _SheetScaffold({required this.child, required this.scrollController});

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;
    const horizontal = 16.0;
    const handleTopGap = 8.0; // od górnej krawędzi arkusza do uchwytu
    const handleToXGap = 8.0; // uchwyt -> X
    const xToHeaderGap = 12.0; // X -> nagłówek (kolorowy marker + tytuł)
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
            // Grip
            const _Grip(),
            const SizedBox(height: handleToXGap),
            // Pasek akcji (tylko X na razie)
            Row(
              children: [
                // Przycisk X w tym samym 48x48 slocie co ikony sekcji (circle 40 dla ikony 24)
                _AdaptiveIconSlot(
                  iconSize: 24,
                  semanticsLabel: 'Zamknij szczegóły zajęć',
                  isButton: true,
                  onTap: () => Navigator.of(context).maybePop(),
                  child: Icon(MyUz.x_close, size: 24, color: const Color(0xFF1D192B)),
                ),
                const Spacer(),
              ],
            ),
            const SizedBox(height: xToHeaderGap),
            // Treść przewijalna
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

/// Właściwa treść (nagłówek + sekcje ikon)
class _DetailsContent extends StatelessWidget {
  final ClassModel classModel;
  const _DetailsContent({required this.classModel});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final typeLabel = _mapType(classModel.type);
    final dateLine = _dateLine(classModel.startTime, classModel.endTime);

    const headerBottomGap = 28.0; // zwiększony odstęp od nagłówka do sekcji szczegółów
    const rowVerticalGap = 12.0;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _AdaptiveIconSlot(
              iconSize: 16, // marker 16 w kole 32
              child: _TypeColorMarker(color: AppColors.myUZSysLightPrimaryContainer),
            ),
            const SizedBox(width: _kIconToTextGap),
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
                    maxLines: 2,
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
          _DetailRow(icon: MyUz.stand, label: typeLabel),
          const SizedBox(height: rowVerticalGap),
        ],
        _DetailRow(icon: MyUz.marker_pin_04, label: classModel.room.isNotEmpty ? classModel.room : 'Sala -'),
        const SizedBox(height: 0), // odstęp zredukowany do 0 na życzenie
        _DetailRow(icon: MyUz.user_01, label: classModel.lecturer.isNotEmpty ? classModel.lecturer : 'Prowadzący -'),
      ],
    );
  }

  // --- FORMATOWANIE DATY / TYP ---

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
      dayLabel = _plWeekday(start.weekday); // np. Środa
    }

    final monthShort = _plMonthShort(start.month);
    final bool showYear = start.year != now.year;
    final datePart = showYear
        ? '$dayLabel, ${start.day} $monthShort ${start.year}'
        : '$dayLabel, ${start.day} $monthShort';

    // en dash (U+2013) z odstępami po bokach jak w Google Calendar
    final timePart = '${_hhmm(start)} – ${_hhmm(end)}';
    return '$datePart • $timePart';
  }

  static String _hhmm(DateTime d) =>
      '${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';

  static String _plWeekday(int weekday) {
    const dni = ['Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota','Niedziela'];
    return dni[weekday - 1];
  }

  static String _plMonthShort(int m) {
    const mies = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];
    return mies[m - 1];
  }

  static String? _mapType(String? raw) {
    if (raw == null) return null;
    final r = raw.trim();
    if (r.isEmpty) return null;
    final u = r.toUpperCase();
    // Pełna mapa skrótów -> nazwy
    switch (u) {
      case 'R': return 'Rezerwacja';
      case 'BHP': return 'Szkolenie BHP';
      case 'C': return 'Ćwiczenia';
      case 'CZ': return 'Ćwiczenia / Zdalne';
      case 'Ć': return 'Ćwiczenia';
      case 'ĆL': return 'Ćwiczenia i laboratorium';
      case 'E': return 'Egzamin';
      case 'E/Z': return 'Egzamin / Zdalne';
      case 'I': return 'Inne';
      case 'K': return 'Konwersatorium';
      case 'L': return 'Laboratorium';
      case 'P': return 'Projekt';
      case 'PRA': return 'Praktyka';
      case 'PRO': return 'Proseminarium';
      case 'PRZ': return 'Praktyka zawodowa';
      case 'P/Z': return 'Projekt / Zdalne';
      case 'S': return 'Seminarium';
      case 'SK': return 'Samokształcenie';
      case 'T': return 'Zajęcia terenowe';
      case 'W': return 'Wykład';
      case 'WAR': return 'Warsztaty';
      case 'W+C': return 'Wykład i ćwiczenia';
      case 'WĆL': return 'Wykład + ćwiczenia + laboratorium';
      case 'W+K': return 'Wykłady + konwersatoria';
      case 'W+L': return 'Wykład i laboratorium';
      case 'W+P': return 'Wykład + projekt';
      case 'WW': return 'Wykład i warsztaty';
      case 'W/Z': return 'Wykład / Zdalne';
      case 'Z': return 'Zdalne';
      case 'ZK': return 'Zajęcia kliniczne';
      case 'ZP': return 'Zajęcia praktyczne';
    }
    // Dotychczasowe skróty (zachowane – mogą wystąpić inną wielkością lub wariantem)
    if (u.startsWith('WYK')) return 'Wykład';
    if (u.startsWith('LAB')) return 'Laboratorium';
    if (u == 'ĆW' || u == 'CW' || u.startsWith('ĆW') || u.startsWith('CW')) return 'Ćwiczenia';
    if (u.startsWith('SEM')) return 'Seminarium';
    if (u.startsWith('PROJ')) return 'Projekt';
    if (u.startsWith('KON')) return 'Konwersatorium';
    if (u == 'WF') return 'Wychowanie fizyczne';
    if (u.startsWith('LEKT')) return 'Lektorat';
    if (u.startsWith('EGZ')) return 'Egzamin';
    if (u.startsWith('KOL')) return 'Kolokwium';
    // fallback – kapitalizacja pierwszej litery reszty
    final lower = r.toLowerCase();
    return lower[0].toUpperCase() + lower.substring(1);
  }
}

/// Uchwyt (grip) – 40x4, radius 2
class _Grip extends StatelessWidget {
  const _Grip();
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

/// Slot ikony w stylu Google: 48x48 hit area, centralne koło (32 lub 40) z ikoną.
class _AdaptiveIconSlot extends StatelessWidget {
  final double iconSize; // 16/20/24
  final Widget child; // glif lub marker
  final VoidCallback? onTap;
  final String? semanticsLabel;
  final bool isButton;
  const _AdaptiveIconSlot({
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
      child: Center(
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

/// Kolorowy marker typu zajęć 16x16, radius 4
class _TypeColorMarker extends StatelessWidget {
  final Color color;
  const _TypeColorMarker({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 16,
      height: 16,
      decoration: ShapeDecoration(
        color: color,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
    );
  }
}

/// Wiersz szczegółu (ikona + tekst)
class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  const _DetailRow({required this.icon, required this.label});
  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _AdaptiveIconSlot(
          iconSize: 20,
          child: Icon(icon, size: 20, color: cs.onSurface),
        ),
        const SizedBox(width: _kIconToTextGap),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.only(top: 14), // optyczne wyrównanie baseline do ikony centrowanej
            child: Text(
              label,
              style: AppTextStyle.myUZBodyLarge.copyWith(color: cs.onSurface),
            ),
          ),
        ),
      ],
    );
  }
}
