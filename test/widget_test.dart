// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:my_uz/main.dart';

void main() {
  testWidgets('App boots and shows HomeScreen (onboarding skipped)', (WidgetTester tester) async {
    // Mockujemy SharedPreferences tak, żeby onboarding uznany był za ukończony
    SharedPreferences.setMockInitialValues({'onboarding_complete': true});

    await tester.pumpWidget(const MyBootstrap());
    await tester.pumpAndSettle();

    // Powinien istnieć któryś z elementów ekranu głównego – np. tekst powitalny fragment "Cześć," lub placeholder sekcji.
    expect(find.textContaining('Cześć,'), findsOneWidget);
  });
}
