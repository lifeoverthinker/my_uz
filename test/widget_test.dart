// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:my_uz/main.dart';

void main() {
  testWidgets('App boots and shows HomeScreen (onboarding skipped)', (WidgetTester tester) async {
    // Wymuszamy ustawienie preferencji onboarding_complete na true (pomijamy realny onboarding)
    // Ponieważ widget test nie uruchamia SharedPreferences normalnie, można pominąć i po prostu
    // odpalić HomePage bezpośrednio – ale tu testujemy start MyBootstrap.

    await tester.pumpWidget(const MyBootstrap());
    await tester.pumpAndSettle();

    // Powinien istnieć któryś z elementów ekranu głównego – np. tekst powitalny fragment "Cześć," lub placeholder sekcji.
    expect(find.textContaining('Cześć,'), findsOneWidget);
  });
}
