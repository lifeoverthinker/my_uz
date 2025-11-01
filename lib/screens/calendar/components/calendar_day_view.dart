// CalendarDayView fixes:
// - clamp animateTo offset to controller's maxScrollExtent to avoid exceptions/overflow
// - minor defensiveness for hasClients usage and animateTo calls
// - fixed malformed Positioned "now" row (removed unterminated string / stray quotes)
// - kept all original layout and behavior otherwise

import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/widgets/cards/class_card.dart';
import 'package:my_uz/screens/home/details/class_details.dart';

class CalendarDayView extends StatefulWidget {
  final DateTime day;
  final List<ClassModel> classes;
  final VoidCallback? onNextDay;
  final VoidCallback? onPrevDay;

  const CalendarDayView({super.key, required this.day, required this.classes, this.onNextDay, this.onPrevDay});

  @override
  State<CalendarDayView> createState() => _CalendarDayViewState();
}

class _CalendarDayViewState extends State<CalendarDayView> {
  late Timer _timer;
  DateTime _now = DateTime.now();
  final ScrollController _scrollController = ScrollController();

  static const int _startHour = 0;
  static const int _endHour = 24;
  static const double _hourHeight = 56;
  static const double _hPad = 16;
  static const double _labelWidth = 48;
  static const double _labelFragmentGap = 4;
  static const double _smallSegment = 8;
  static const double _topPad = 8.0;
  static const double _bottomPad = 12.0;

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(minutes: 1), (_) {
      if (mounted) setState(() => _now = DateTime.now());
    });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      _scrollToInitial();
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    _scrollController.dispose();
    super.dispose();
  }

  bool _sameDay(DateTime a, DateTime b) {
    final la = a.toLocal();
    final lb = b.toLocal();
    return la.year == lb.year && la.month == lb.month && la.day == lb.day;
  }

  Future<void> _scrollToInitial() async {
    if (!mounted) return;
    final dayClasses = widget.classes.where((c) => _sameDay(c.startTime, widget.day)).toList()
      ..sort((a, b) => a.startTime.compareTo(b.startTime));
    double targetHour;
    if (dayClasses.isNotEmpty) {
      targetHour = (dayClasses.first.startTime.toLocal().hour - 1).clamp(_startHour, _endHour - 1).toDouble();
    } else if (_sameDay(widget.day, DateTime.now())) {
      targetHour = 7;
    } else {
      targetHour = 8;
    }
    final offset = targetHour * _hourHeight;

    // Defensive: wait for controller to have a position/clients, clamp to maxScrollExtent
    if (_scrollController.hasClients) {
      try {
        final max = _scrollController.position.maxScrollExtent;
        final safeOffset = offset.clamp(0.0, max);
        final current = _scrollController.offset;
        if ((current - safeOffset).abs() > 8) {
          await _scrollController.animateTo(safeOffset, duration: const Duration(milliseconds: 450), curve: Curves.easeOutCubic);
        }
      } catch (_) {
        // ignore animation errors
      }
    } else {
      // If no clients yet, schedule another attempt after a frame
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _scrollToInitial();
      });
    }
  }

  double _timeToPixels(DateTime time) {
    final localDayStart = DateTime(widget.day.year, widget.day.month, widget.day.day, 0, 0, 0);
    final clamped = time.toLocal().isBefore(localDayStart)
        ? localDayStart
        : (time.toLocal().isAfter(localDayStart.add(const Duration(hours: 24))) ? localDayStart.add(const Duration(hours: 24)) : time.toLocal());
    final minutes = clamped.difference(localDayStart).inMinutes;
    return minutes / 60 * _hourHeight;
  }

  Color _backgroundForIndex(int localIndex) {
    return localIndex.isEven ? const Color(0xFFE8DEF8) : const Color(0xFFFFD8E4);
  }

  Widget _buildPositionedCard(ClassModel c, int index, {required double verticalLineX}) {
    final startPos = _timeToPixels(c.startTime) + _topPad;
    final endPos = _timeToPixels(c.endTime) + _topPad;
    final double height = (endPos - startPos).clamp(40.0, 4000.0).toDouble();
    final bool finished = DateTime.now().toLocal().isAfter(c.endTime.toLocal());

    final card = GestureDetector(
      onTap: () => ClassDetailsSheet.open(context, c),
      child: ClassCard.calendar(
        title: c.subject,
        time: '${DateFormat('HH:mm').format(c.startTime.toLocal())} - ${DateFormat('HH:mm').format(c.endTime.toLocal())}',
        room: c.room,
        statusDotColor: finished ? Colors.transparent : const Color(0xFF7D5260),
        showStatusDot: !finished,
        hugHeight: false,
        backgroundColor: _backgroundForIndex(index),
      ),
    );

    const double rightGap = 8.0;

    return Positioned(
      top: startPos + 1,
      left: verticalLineX + 1,
      right: rightGap,
      height: height,
      child: Opacity(opacity: finished ? 0.55 : 1, child: card),
    );
  }

  String _hourLabel(int hour) => '${hour.toString().padLeft(2, '0')}:00';

  @override
  void didUpdateWidget(covariant CalendarDayView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!_sameDay(oldWidget.day, widget.day) || oldWidget.classes != widget.classes) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToInitial());
    }
  }

  @override
  Widget build(BuildContext context) {
    final dayClasses = widget.classes.where((c) => _sameDay(c.startTime, widget.day)).toList()..sort((a, b) => a.startTime.compareTo(b.startTime));
    final double totalHeight = _hourHeight * 24;
    final double labelHalfHeight = 10.0;
    final double shortFragmentLeft = _labelWidth + _labelFragmentGap;
    final double verticalLineX = _labelWidth + _labelFragmentGap + _smallSegment;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onHorizontalDragEnd: (details) {
        final v = details.primaryVelocity ?? 0;
        if (v.abs() < 200) return;
        if (v < 0) {
          widget.onNextDay?.call();
        } else {
          widget.onPrevDay?.call();
        }
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: _hPad),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final height = math.max(totalHeight, constraints.maxHeight - 8);
            return SingleChildScrollView(
              controller: _scrollController,
              padding: EdgeInsets.zero,
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: height + _topPad + _bottomPad),
                child: IntrinsicHeight(
                  child: Stack(
                    clipBehavior: Clip.none,
                    children: [
                      for (int i = 1; i < 24; i++)
                        Positioned(top: i * _hourHeight + _topPad, left: shortFragmentLeft, width: _smallSegment, child: Container(height: 1, color: const Color(0xFFEDE6F3))),

                      Positioned(top: _topPad, bottom: _bottomPad, left: verticalLineX, width: 1, child: Container(color: const Color(0xFFEDE6F3))),

                      for (int i = 1; i < 24; i++)
                        Positioned(top: i * _hourHeight + _topPad, left: verticalLineX + 1, right: 0, child: Container(height: 1, color: const Color(0xFFEDE6F3))),

                      for (int i = 1; i < 24; i++)
                        Positioned(
                          top: (i * _hourHeight) - labelHalfHeight + _topPad,
                          left: 0,
                          width: _labelWidth,
                          child: Align(
                            alignment: Alignment.centerLeft,
                            child: Text(_hourLabel(i), maxLines: 1, style: Theme.of(context).textTheme.labelSmall?.copyWith(color: const Color(0xFF494949), fontWeight: FontWeight.w500)),
                          ),
                        ),

                      for (int i = 0; i < dayClasses.length; i++) _buildPositionedCard(dayClasses[i], i, verticalLineX: verticalLineX),

                      if (_sameDay(widget.day, _now))
                        Positioned(
                          top: _timeToPixels(_now) - 1 + _topPad,
                          left: verticalLineX - 5,
                          right: 0,
                          child: Row(children: [
                            SizedBox(width: 10, child: Align(alignment: Alignment.centerRight, child: _NowDot(color: Colors.red))),
                            Expanded(child: Container(height: 2, color: Colors.red)),
                          ]),
                        ),
                    ],
                  ),
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
    return Container(width: 10, height: 10, decoration: BoxDecoration(color: color, shape: BoxShape.circle, border: Border.all(color: Colors.white, width: 2)));
  }
}