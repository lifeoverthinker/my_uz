import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:my_uz/screens/calendar/components/calendar_view.dart';
import 'package:my_uz/screens/calendar/components/calendar_day_view.dart';
import 'package:my_uz/models/class_model.dart';
import 'package:my_uz/widgets/cards/class_card.dart';

void main() {
  test('buildMonthDays returns 42 consecutive unique days starting on Monday', () {
    final days = CalendarView.buildMonthDays(DateTime(2025, 10, 1));
    expect(days.length, 42);
    expect(days.first.weekday, 1); // musi zaczynać się od poniedziałku
    // unikalność
    expect(days.toSet().length, 42);
    // kolejność rosnąca o 1 dzień
    for (var i = 0; i < days.length - 1; i++) {
      expect(days[i + 1].difference(days[i]).inDays, 1);
    }
  });

  testWidgets('CalendarDayView: class card right edge is 8px from frame', (WidgetTester tester) async {
    const frameWidth = 400.0;
    final classModel = ClassModel(
      id: 'c1',
      subject: 'Test',
      room: 'R1',
      lecturer: 'L',
      startTime: DateTime(2025, 10, 10, 10, 0),
      endTime: DateTime(2025, 10, 10, 11, 0),
    );

    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: Center(
          child: SizedBox(
            width: frameWidth,
            child: CalendarDayView(
              day: DateTime(2025, 10, 10),
              classes: [classModel],
            ),
          ),
        ),
      ),
    ));

    // poczekaj na animacje/auto-scroll
    await tester.pumpAndSettle();

    // znajdź ClassCard (powinien być widget karty w stacku)
    final cardFinder = find.byType(ClassCard);
    expect(cardFinder, findsOneWidget);

    // oblicz prawą krawędź kontenera CalendarDayView
    final containerFinder = find.byType(CalendarDayView);
    expect(containerFinder, findsOneWidget);
    final containerRect = tester.getRect(containerFinder);

    final cardRect = tester.getRect(cardFinder);
    final rightGap = containerRect.right - cardRect.right;

    // prawa krawędź powinna być 8px od prawej ramki (z tolerancją 1.5 px)
    expect((rightGap - 8.0).abs(), lessThanOrEqualTo(1.5));
  });

  testWidgets('CalendarView responds to horizontal swipe to change week', (WidgetTester tester) async {
    bool nextCalled = false;
    bool prevCalled = false;

    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: SizedBox(
          width: 360,
          child: CalendarView(
            focusedDay: DateTime(2025, 10, 15),
            selectedDay: DateTime(2025, 10, 15),
            isWeekView: true,
            classesByDay: const {},
            onDaySelected: (_) {},
            onNextWeek: () => nextCalled = true,
            onPrevWeek: () => prevCalled = true,
          ),
        ),
      ),
    ));

    await tester.pumpAndSettle();

    // swipe left (should call next)
    await tester.fling(find.byType(CalendarView), const Offset(-200, 0), 1000);
    await tester.pumpAndSettle();
    expect(nextCalled, isTrue);

    // swipe right (should call prev)
    await tester.fling(find.byType(CalendarView), const Offset(200, 0), 1000);
    await tester.pumpAndSettle();
    expect(prevCalled, isTrue);
  });

  testWidgets('Swiping on header triggers week change', (WidgetTester tester) async {
    bool nextCalled = false;
    bool prevCalled = false;

    // Replikuje wrapper z CalendarScreen: header (SizedBox) + CalendarView
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onHorizontalDragEnd: (details) {
            final v = details.primaryVelocity ?? 0;
            if (v.abs() < 40) return;
            if (v < 0) {
              nextCalled = true;
            } else {
              prevCalled = true;
            }
          },
          child: Column(
            children: [
              const SizedBox(height: 24, key: Key('header')),
              SizedBox(
                height: 100,
                child: CalendarView(
                  focusedDay: DateTime(2025, 10, 15),
                  selectedDay: DateTime(2025, 10, 15),
                  isWeekView: true,
                  classesByDay: const {},
                  onDaySelected: (_) {},
                ),
              ),
            ],
          ),
        ),
      ),
    ));

    await tester.pumpAndSettle();

    // swipe left on header
    await tester.fling(find.byKey(const Key('header')), const Offset(-200, 0), 1000);
    await tester.pumpAndSettle();
    expect(nextCalled, isTrue);

    // swipe right on header
    nextCalled = false;
    await tester.fling(find.byKey(const Key('header')), const Offset(200, 0), 1000);
    await tester.pumpAndSettle();
    expect(prevCalled, isTrue);
  });
}
