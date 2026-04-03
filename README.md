# MyUZ Android

Nowoczesna aplikacja mobilna dla studentów Uniwersytetu Zielonogórskiego.
Projekt jest rozwijany w Kotlinie z UI w Jetpack Compose.

## Funkcjonalności

### Home
- podsumowanie dnia,
- najbliższe zajęcia,
- zadania,
- wydarzenia.

### Kalendarz
- własny plan zajęć,
- wyszukiwanie planów (`Grupy` / `Wykładowcy`),
- podgląd planu,
- dodawanie planów do ulubionych,
- filtrowanie podgrup.

### Indeks
- oceny,
- nieobecności,
- szczegóły i formularze dodawania/edycji.

### Dodatkowe moduły
- onboarding przy pierwszym uruchomieniu,
- ekran powiadomień,
- ustawienia motywu i danych,
- widget z najbliższymi zajęciami (Glance + WorkManager).

## Stack technologiczny

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Room
- Coroutines + Flow
- WorkManager
- Glance App Widget
- Supabase (API / dane planu)
- Manual DI (kontener aplikacji)

## Architektura

- MVVM
- Repozytoria + lokalna baza danych
- Warstwa UI przygotowana pod Light/Dark mode przez `MaterialTheme`

## Wymagania

- Android Studio (Flamingo/Hedgehog/Koala lub nowsze)
- JDK 17
- Android SDK (minSdk 26, targetSdk 34)

## Konfiguracja lokalna

Uzupełnij `local.properties`:

```properties
SUPABASE_URL=https://twoj-projekt.supabase.co
SUPABASE_KEY=twoj_klucz
```

## Uruchomienie

```powershell
Set-Location "C:\Users\Martyna\Documents\GitHub\my_uz_android"
.\gradlew.bat :app:assembleDebug
```

APK debug:

- `app/build/outputs/apk/debug/`

## Testy

Wszystkie testy jednostkowe:

```powershell
Set-Location "C:\Users\Martyna\Documents\GitHub\my_uz_android"
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

Wybrane testy:

```powershell
Set-Location "C:\Users\Martyna\Documents\GitHub\my_uz_android"
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.my_uz_android.CalendarViewModelTest" --no-daemon
```

## Widget

Pliki modułu widgetu:

- `app/src/main/java/com/example/my_uz_android/widget/Widget.kt`
- `app/src/main/java/com/example/my_uz_android/widget/WidgetWorker.kt`
- `app/src/main/java/com/example/my_uz_android/widget/WidgetReceiver.kt`

Aktualizacja widgetu:
- ręczna (przycisk odświeżania),
- cykliczna (WorkManager),
- wymuszana przy wejściu do aplikacji.

## Dokumentacja kodu (KDoc)

Aktualny postęp folderami:

- zaliczone: `ui/components`, `ui/theme`, `ui/screens/onboarding`
- w toku: kolejne foldery `ui/screens`

## Status projektu

Projekt nieoficjalny (nie jest oficjalną aplikacją UZ).
