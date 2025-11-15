import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/widgets/cards/class_card.dart';
import 'package:my_uz/screens/home/details/class_details.dart';

/// Widok dzienny: siatka godzin + karty zajęć rozmieszczone pionowo.
class CalendarDayView extends StatefulWidget {
  final DateTime day;
  final List<ClassModel> classes;
  final VoidCallback? onNextDay; // swipe right
  final VoidCallback? onPrevDay; // swipe left
  const CalendarDayView({super.key, required this.day, required this.classes, this.onNextDay, this.onPrevDay});

  @override
  State<CalendarDayView> createState() => _CalendarDayViewState();
}

class _CalendarDayViewState extends State<CalendarDayView> {
  late Timer _timer;
  DateTime _now = DateTime.now();
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(minutes: 1), (_) { if (mounted) setState(() => _now = DateTime.now()); });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final dayClasses = widget.classes.where((c)=>_sameDay(c.startTime, widget.day)).toList();
      if (dayClasses.isNotEmpty) {
        dayClasses.sort((a,b)=>a.startTime.compareTo(b.startTime));
        final first = dayClasses.first.startTime;
        final targetHour = (first.hour - 1).clamp(_startHour, _defaultEndHour - 1);
        final offset = targetHour * _hourHeight; // bez topGap, pełna siatka
        if (_scrollController.hasClients) {
          _scrollController.animateTo(offset.toDouble(), duration: const Duration(milliseconds: 450), curve: Curves.easeOutCubic);
        }
      } else if (_sameDay(widget.day, DateTime.now())) {
        const baseHour = 7;
        const offset = baseHour * _hourHeight;
        if (_scrollController.hasClients) {
          _scrollController.animateTo(offset.toDouble(), duration: const Duration(milliseconds: 450), curve: Curves.easeOutCubic);
        }
      }
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    _scrollController.dispose();
    super.dispose();
  }

  static const int _startHour = 0; // pełna doba
  static const int _defaultEndHour = 24; // koniec doby
  static const double _hourHeight = 56; // mniejsze, by 24h było wygodniejsze w scrollu
  // padding otaczający treść
  static const double _hPad = 16;
  // szerokość obszaru etykiet godzin (poza paddingiem)
  static const double _labelWidth = 48; // mniejsza szerokość, godziny przylegają do lewej
  // odstęp między etykietą a krótkim fragmentem linii
  // Zmniejszony gap zgodnie z prośbą: bliżej wystającego krótkiego fragmentu
  static const double _labelFragmentGap = 4;
  // długość krótkiego fragmentu poziomej linii przy kolumnie godzin
  static const double _smallSegment = 8;

  bool _sameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

  @override
  void didUpdateWidget(covariant CalendarDayView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!_sameDay(oldWidget.day, widget.day)) {
      // po zmianie dnia – animowany autoscroll
      WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToInitial());
    } else if (oldWidget.classes != widget.classes) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToInitial());
    }
  }

  Future<void> _scrollToInitial() async {
    if (!mounted) return;
    final dayClasses = widget.classes.where((c)=>_sameDay(c.startTime, widget.day)).toList()
      ..sort((a,b)=>a.startTime.compareTo(b.startTime));
    double targetHour;
    if (dayClasses.isNotEmpty) {
      targetHour = (dayClasses.first.startTime.hour - 1).clamp(_startHour, _defaultEndHour - 1).toDouble();
    } else if (_sameDay(widget.day, DateTime.now())) {
      targetHour = 7;
    } else {
      targetHour = 8; // neutralny środek
    }
    final offset = targetHour * _hourHeight; // pełna skala 0..24
    if (_scrollController.hasClients) {
      final current = _scrollController.offset;
      if ((current - offset).abs() > 8) {
        _scrollController.animateTo(
          offset.toDouble(),
          duration: const Duration(milliseconds: 450),
          curve: Curves.easeOutCubic,
        );
      }
    }
  }

  double _timeToPixels(DateTime time) {
    final dayStart = DateTime(widget.day.year, widget.day.month, widget.day.day, 0);
    final clamped = time.isBefore(dayStart)
        ? dayStart
        : (time.isAfter(dayStart.add(const Duration(hours: 24)))
            ? dayStart.add(const Duration(hours: 24))
            : time);
    final minutes = clamped.difference(dayStart).inMinutes;
    return minutes / 60 * _hourHeight; // 0:00 -> 0, 1:00 -> _hourHeight, 23:00 -> 23*_hourHeight
  }

  Color _backgroundForIndex(int localIndex) {
    // Naprzemienne kolory jak w makiecie: fioletowy (#E8DEF8) i różowy (#FFD8E4)
    return localIndex.isEven ? const Color(0xFFE8DEF8) : const Color(0xFFFFD8E4);
  }

  Widget _buildPositionedCard(ClassModel c, int index, {double topPad = 0, required double containerWidth}) {
    final startPos = _timeToPixels(c.startTime) + topPad;
    final endPos = _timeToPixels(c.endTime) + topPad;
    final double height = (endPos - startPos).clamp(40, 4000).toDouble();
    final bool finished = DateTime.now().isAfter(c.endTime);
    final card = GestureDetector(
      onTap: () => ClassDetailsSheet.open(context, c),
      child: ClassCard.calendar(
        title: c.subject,
        time: '${DateFormat('HH:mm').format(c.startTime)} - ${DateFormat('HH:mm').format(c.endTime)}',
        room: c.room,
        statusDotColor: finished ? Colors.transparent : const Color(0xFF7D5260),
        showStatusDot: !finished,
        hugHeight: false,
        backgroundColor: _backgroundForIndex(index),
      ),
    );
    const verticalLineX = _labelWidth + _labelFragmentGap + _smallSegment;
    const cardLeft = verticalLineX + 1; // przylega do linii pionowej
    // szerokość dostępna: containerWidth - cardLeft - prawy padding (=_hPad)
    const double safeMargin = 4.0; // zapas, żeby zaokrąglony róg nie był przycinany
    final availableWidth = (containerWidth - cardLeft - _hPad - safeMargin).clamp(80.0, containerWidth);
    return Positioned(
      top: startPos + 1,
      left: cardLeft,
      width: availableWidth,
      height: height,
      child: Opacity(opacity: finished ? 0.55 : 1, child: card),
    );
  }

  String _hourLabel(int hour) => '${hour.toString().padLeft(2, '0')}:00';

  @override
  Widget build(BuildContext context) {
    // helper variables used for layout
    final dayClasses = widget.classes.where((c) => _sameDay(c.startTime, widget.day)).toList()..sort((a,b)=>a.startTime.compareTo(b.startTime));
    final double totalHeight = _hourHeight * 24; // full 24h grid height
    final double topPad = 8.0; // spacing at top
    final double bottomPad = 12.0; // spacing at bottom
    final double labelHalfHeight = 10.0; // used to vertically center hour labels
    final double shortFragmentLeft = _labelWidth + _labelFragmentGap; // left offset for short fragments
    final double verticalLineX = _labelWidth + _labelFragmentGap + _smallSegment; // x for vertical timeline line

    // GestureDetector around whole view to detect horizontal swipes and change day
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onHorizontalDragEnd: (details) {
        final v = details.primaryVelocity ?? 0;
        if (v.abs() < 200) return; // ignore small flings
        if (v < 0) {
          widget.onNextDay?.call();
        } else {
          widget.onPrevDay?.call();
        }
      },
      child: Padding(
        // Przywróć padding 16 po obu stronach; wewnętrzne etykiety godzin będą przylegać do lewej wewnątrz tej przestrzeni.
        padding: const EdgeInsets.symmetric(horizontal: _hPad),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final containerWidth = constraints.maxWidth;
            final height = math.max(totalHeight.toDouble(), constraints.maxHeight);
            return SingleChildScrollView(
              controller: _scrollController,
              padding: EdgeInsets.zero,
              child: SizedBox(
                height: height + topPad + bottomPad,
                child: Stack(
                  clipBehavior: Clip.none,
                  children: [
                    // Poziome krótkie segmenty (pozostawione – jeśli niepotrzebne można usunąć) przesunięte o topPad
                    for (int i = 1; i < 24; i++)
                      Positioned(
                        top: i * _hourHeight + topPad,
                        left: shortFragmentLeft,
                        width: _smallSegment,
                        child: Container(height: 1, color: const Color(0xFFEDE6F3)),
                      ),
                    // Linia pionowa
                    Positioned(
                      top: topPad,
                      bottom: bottomPad,
                      left: verticalLineX,
                      width: 1,
                      child: Container(color: const Color(0xFFEDE6F3)),
                    ),
                    // Poziome dalsze linie
                    for (int i = 1; i < 24; i++)
                      Positioned(
                        top: i * _hourHeight + topPad,
                        left: verticalLineX + 1,
                        right: 0,
                        child: Container(height: 1, color: const Color(0xFFEDE6F3)),
                      ),
                    // Etykiety godzin (przesunięte) — tekst wyrównany do lewej (przylega do lewej krawędzi)
                    for (int i = 1; i < 24; i++)
                      Positioned(
                        top: (i * _hourHeight) - labelHalfHeight + topPad,
                        left: 0,
                        width: _labelWidth,
                        child: Padding(
                          padding: const EdgeInsets.only(left: 0),
                          child: Align(
                            alignment: Alignment.centerLeft,
                            child: Text(
                              _hourLabel(i),
                              maxLines: 1,
                              softWrap: false,
                              overflow: TextOverflow.ellipsis,
                              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                color: const Color(0xFF494949),
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ),
                      ),
                    // Karty zajęć
                    for (final _idx in List<int>.generate(dayClasses.length, (i) => i))
                      _buildPositionedCard(dayClasses[_idx], _idx, topPad: topPad, containerWidth: containerWidth),
                    // Linia "teraz"
                    if (_sameDay(widget.day, _now))
                      Positioned(
                        top: _timeToPixels(_now) - 1 + topPad,
                        left: verticalLineX - 5,
                        right: 0,
                        child: Row(
                          children: [
                            const SizedBox(
                              width: 10,
                              child: Align(
                                alignment: Alignment.centerRight,
                                child: _NowDot(color: Colors.red),
                              ),
                            ),
                            Expanded(child: Container(height: 2, color: Colors.red)),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _NowDot extends StatelessWidget {
  final Color color;
  const _NowDot({required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 10,
      height: 10,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        border: Border.all(color: Colors.white, width: 2),
      ),
    );
  }
}
