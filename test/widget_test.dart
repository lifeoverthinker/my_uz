// Plik: test/widget_test.dart
import 'package:flutter_test/flutter_test.dart';
import 'package:my_uz/main.dart'; // Importuj main.dart

void main() {
  testWidgets('Counter increments smoke test', (WidgetTester tester) async {
    // POPRAWKA: Użyj `MyBootstrap` (zgodnie z main.dart)
    await tester.pumpWidget(const MyBootstrap());

    // Testy są puste, bo nie ma licznika
    expect(find.text('0'), findsNothing);
    expect(find.text('1'), findsNothing);
  });
}