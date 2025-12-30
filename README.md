<div align="center">

# 🎓 MyUZ Android

### Aplikacja mobilna dla studentów Uniwersytetu Zielonogórskiego

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF? style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=material-design&logoColor=white)](https://m3.material.io/)

*Zarządzaj swoim życiem studenckim na UZ w jednej aplikacji* ✨

</div>

---

## 📋 O aplikacji

**MyUZ Android** to mobilna aplikacja stworzona specjalnie dla studentów **Uniwersytetu Zielonogórskiego**. 

### 🎯 Cel projektu

Aplikacja powstała z potrzeby stworzenia **kompleksowego narzędzia** do zarządzania życiem studenckim.  Zamiast korzystać z wielu różnych platform, kalendarzy i notatników - wszystko masz w jednym miejscu, zawsze pod ręką.

### 💡 Dlaczego MyUZ? 

<table>
<tr>
<td width="33%" align="center">
<h3>🎓</h3>
<b>Dedykowana dla UZ</b><br/>
Stworzona z myślą o specyfice Uniwersytetu Zielonogórskiego
</td>
<td width="33%" align="center">
<h3>📱</h3>
<b>Zawsze pod ręką</b><br/>
Twój plan, oceny i zadania w telefonie
</td>
<td width="33%" align="center">
<h3>☁️</h3>
<b>Synchronizacja</b><br/>
Dostęp do danych z dowolnego urządzenia
</td>
</tr>
<tr>
<td width="33%" align="center">
<h3>🎨</h3>
<b>Nowoczesny design</b><br/>
Intuicyjny interfejs oparty na Material Design 3
</td>
<td width="33%" align="center">
<h3>🔒</h3>
<b>Prywatność</b><br/>
Dane przechowywane lokalnie i szyfrowane w chmurze
</td>
<td width="33%" align="center">
<h3>⚡</h3>
<b>Szybka i responsywna</b><br/>
Zbudowana na najnowszych technologiach Android
</td>
</tr>
</table>

### 🌟 Co wyróżnia tę aplikację? 

- ✅ **Wszystko w jednym** - Plan, oceny, frekwencja i zadania w jednej apce
- ✅ **Offline-first** - Działa bez internetu, synchronizuje gdy jest połączenie
- ✅ **Automatyczne obliczenia** - Średnie ważone, statystyki, procenty frekwencji
- ✅ **Personalizacja** - Jasny/ciemny motyw, konfigurowalne powiadomienia
- ✅ **Clean Architecture** - Stabilna, skalowalna i łatwa w utrzymaniu
- ✅ **100% Kotlin** - Nowoczesny kod napisany w całości w Kotlinie

---

## ✨ Funkcje

<table>
<tr>
<td width="50%">

### 📅 Plan zajęć
- ✓ Przejrzysty kalendarz z harmonogramem wszystkich zajęć
- ✓ Widok tygodniowy i dzienny
- ✓ Szczegóły zajęć:  sala, prowadzący, godziny
- ✓ Synchronizacja z chmurą Supabase

</td>
<td width="50%">

### 📊 Oceny
- ✓ Śledzenie wszystkich ocen z przedmiotów
- ✓ Automatyczne obliczanie średniej ważonej
- ✓ Szczegółowe informacje o każdym przedmiocie
- ✓ Wizualizacja postępów w nauce

</td>
</tr>
<tr>
<td width="50%">

### 🚫 Nieobecności
- ✓ Monitoring frekwencji na zajęciach
- ✓ Procentowa frekwencja dla każdego przedmiotu
- ✓ Historia wszystkich nieobecności
- ✓ Ostrzeżenia o przekroczeniu dopuszczalnego limitu

</td>
<td width="50%">

### ✅ Zadania (To-Do)
- ✓ Lista zadań domowych i projektów
- ✓ Integracja z kalendarzem zajęć
- ✓ Ustawianie priorytetów i terminów
- ✓ Oznaczanie wykonanych zadań

</td>
</tr>
<tr>
<td colspan="2">

### ⚙️ Ustawienia i personalizacja
- ✓ Jasny i ciemny motyw aplikacji
- ✓ Edycja danych studenta (imię, nazwisko, nr albumu)
- ✓ Konfiguracja powiadomień
- ✓ Zarządzanie danymi i synchronizacją

</td>
</tr>
</table>

---

## 📲 Instalacja

### Wymagania
- **Android**:  7.0 (API 24) lub nowszy
- **Pamięć**: ~50 MB

### Opcja 1: Pobierz gotowe APK (najłatwiejsza)

> **Uwaga:** Jeśli nie ma jeszcze żadnego release, sprawdź [GitHub Actions](https://github.com/lifeoverthinker/my_uz_android/actions) - tam znajdziesz automatycznie zbudowane APK z ostatniego commita. 

**Kroki:**

1. Przejdź do zakładki [**Releases**](https://github.com/lifeoverthinker/my_uz_android/releases)
2. Pobierz najnowszy plik APK
3. Na telefonie włącz **Instalację z nieznanych źródeł**: 
   - `Ustawienia → Zabezpieczenia → Nieznane źródła` (lub podobnie)
4. Otwórz pobrany plik APK i zainstaluj

### Opcja 2: Zbuduj z kodu źródłowego

**Wymagania:**
- Android Studio Hedgehog (2023.1.1) lub nowsze
- JDK 17+
- Android SDK (API 24+)

**Kroki:**

```bash
# 1. Sklonuj repozytorium
git clone https://github.com/lifeoverthinker/my_uz_android.git

# 2. Przejdź do katalogu
cd my_uz_android

# 3. Otwórz w Android Studio
# File → Open → wybierz folder my_uz_android
```

4. Poczekaj na synchronizację Gradle (może potrwać kilka minut)
5. Podłącz telefon lub uruchom emulator
6. Kliknij **Run** ▶️ (lub `Shift + F10`)

### Opcja 3: Buduj z terminala

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (wymaga podpisania)
./gradlew assembleRelease

# APK znajdziesz w:  app/build/outputs/apk/debug/ lub app/build/outputs/apk/release/
```

---

## 🛠️ Technologie

Aplikacja została zbudowana z wykorzystaniem nowoczesnych technologii Android:

```
📦 Tech Stack
├── 🎨 UI
│   ├── Jetpack Compose (100%)
│   └── Material Design 3
├── 🏗️ Architektura
│   ├── MVVM Pattern
│   └── Clean Architecture
├── 💾 Baza danych
│   └── Room Database
├── ☁️ Backend
│   └── Supabase
├── ⚡ Asynchroniczność
│   ├── Kotlin Coroutines
│   └── Flow
└── 🔧 Inne
    ├── 100% Kotlin
    └── Manual DI
```

---

## 📖 Jak korzystać

### Pierwsze uruchomienie

1. **Zaloguj się** - podaj swoje dane studenta UZ
2. **Uzupełnij profil** - imię, nazwisko, kierunek, rok studiów
3. **Zsynchronizuj dane** - aplikacja pobierze Twój plan zajęć

### Nawigacja

Aplikacja posiada dolny pasek nawigacji z głównymi sekcjami:  

```
🏠 Główna       - Dashboard z podsumowaniem
📅 Kalendarz    - Plan zajęć
📊 Oceny        - Twoje wyniki
🚫 Nieobecności - Frekwencja
✅ Zadania      - Lista to-do
⚙️ Ustawienia   - Konfiguracja
```

### Dodawanie danych

- **Oceny**:  Kliknij `+` w zakładce Oceny → wybierz przedmiot → wpisz ocenę
- **Zadania**: Kliknij `+` w zakładce Zadania → wpisz tytuł i termin
- **Nieobecności**: Automatycznie śledzone lub dodaj ręcznie

### Synchronizacja

Dane są automatycznie synchronizowane z chmurą Supabase po każdej zmianie.  Możesz korzystać z aplikacji na wielu urządzeniach.

---

## ❓ FAQ

<details>
<summary><b>❓ Czy aplikacja jest oficjalna?</b></summary>
<br>
Nie, to nieoficjalny projekt studencki dla UZ. 
</details>

<details>
<summary><b>🔒 Czy dane są bezpieczne?</b></summary>
<br>
Tak, dane są przechowywane lokalnie (Room) i opcjonalnie w Supabase z szyfrowaniem.
</details>

<details>
<summary><b>📡 Czy mogę używać offline?</b></summary>
<br>
Tak, wszystkie dane są dostępne offline.  Synchronizacja następuje gdy pojawi się internet.
</details>

<details>
<summary><b>🔄 Jak zaktualizować aplikację?</b></summary>
<br>
Pobierz nową wersję z zakładki Releases i zainstaluj na istniejącą.
</details>

<details>
<summary><b>🐛 Gdzie zgłosić błąd?</b></summary>
<br>
W zakładce <a href="https://github.com/lifeoverthinker/my_uz_android/issues">Issues</a> tego repozytorium. 
</details>

<details>
<summary><b>📦 Gdzie znajdę APK jeśli nie ma Releases?</b></summary>
<br>
Sprawdź <a href="https://github.com/lifeoverthinker/my_uz_android/actions">GitHub Actions</a> - workflow automatycznie buduje APK przy każdym pushu.  Kliknij w najnowszy workflow run i pobierz artifact.
</details>

---

## 🔧 Rozwiązywanie problemów

### ❌ Aplikacja nie chce się zainstalować
- Upewnij się, że masz Androida 7.0+
- Włącz **"Instalację z nieznanych źródeł"** w ustawieniach
- Sprawdź czy masz wystarczająco miejsca (~50 MB)

### 🔑 Nie mogę się zalogować
- Sprawdź połączenie z internetem
- Upewnij się, że wprowadzasz poprawne dane UZ

### ☁️ Dane się nie synchronizują
- Sprawdź połączenie z internetem
- Sprawdź ustawienia synchronizacji w aplikacji
- Spróbuj wylogować się i zalogować ponownie

---

<div align="center">

**Autor:** [@lifeoverthinker](https://github.com/lifeoverthinker)

Stworzone z ❤️ dla studentów UZ przy użyciu Kotlin & Jetpack Compose

⭐ Przydatna aplikacja?  Zostaw gwiazdkę! 

</div>