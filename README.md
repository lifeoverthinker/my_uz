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

**MyUZ Android** to mobilna aplikacja stworzona specjalnie dla studentów **Uniwersytetu Zielonogórskiego**. Pozwala na wygodne zarządzanie planem zajęć, śledzenie ocen, monitorowanie frekwencji oraz organizację zadań - wszystko w nowoczesnym, intuicyjnym interfejsie. 

## ✨ Funkcje

### 📅 Plan zajęć
- Przejrzysty kalendarz z harmonogramem wszystkich zajęć
- Widok tygodniowy i dzienny
- Szczegóły zajęć:  sala, prowadzący, godziny
- Synchronizacja z chmurą Supabase

### 📊 Oceny
- Śledzenie wszystkich ocen z przedmiotów
- Automatyczne obliczanie średniej ważonej
- Szczegółowe informacje o każdym przedmiocie
- Wizualizacja postępów w nauce

### 🚫 Nieobecności
- Monitoring frekwencji na zajęciach
- Procentowa frekwencja dla każdego przedmiotu
- Historia wszystkich nieobecności
- Ostrzeżenia o przekroczeniu dopuszczalnego limitu

### ✅ Zadania (To-Do)
- Lista zadań domowych i projektów
- Integracja z kalendarzem zajęć
- Ustawianie priorytetów i terminów
- Oznaczanie wykonanych zadań

###⚙️ Ustawienia i personalizacja
- Jasny i ciemny motyw aplikacji
- Edycja danych studenta (imię, nazwisko, nr albumu)
- Konfiguracja powiadomień
- Zarządzanie danymi i synchronizacją

## 📲 Instalacja

### Wymagania
- **Android**:  7.0 (API 24) lub nowszy
- **Pamięć**:  ~50 MB

### Opcja 1: Pobierz gotowe APK (najłatwiejsza)

1. Przejdź do zakładki [**Releases**](https://github.com/lifeoverthinker/my_uz_android/releases)
2. Pobierz najnowszy plik `MyUZ-vX.X.X.apk`
3. Zainstaluj na swoim telefonie (może wymagać włączenia instalacji z nieznanych źródeł)

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

# APK znajdziesz w:  app/build/outputs/apk/
```

## 🛠️ Technologie

Aplikacja została zbudowana z wykorzystaniem nowoczesnych technologii Android:

| Komponent | Technologia |
|-----------|-------------|
| **Język** | Kotlin 100% |
| **UI** | Jetpack Compose + Material Design 3 |
| **Architektura** | MVVM + Clean Architecture |
| **Baza danych** | Room Database |
| **Backend** | Supabase (synchronizacja w chmurze) |
| **Asynchroniczność** | Kotlin Coroutines + Flow |
| **DI** | Manual Dependency Injection |

## 📖 Jak korzystać

### Pierwsze uruchomienie

1. **Zaloguj się** - podaj swoje dane studenta UZ
2. **Uzupełnij profil** - imię, nazwisko, kierunek, rok studiów
3. **Zsynchronizuj dane** - aplikacja pobierze Twój plan zajęć

### Nawigacja

Aplikacja posiada dolny pasek nawigacji z głównymi sekcjami: 
- 🏠 **Główna** - dashboard z podsumowaniem
- 📅 **Kalendarz** - plan zajęć
- 📊 **Oceny** - Twoje wyniki
- 🚫 **Nieobecności** - frekwencja
- ✅ **Zadania** - lista to-do
- ⚙️ **Ustawienia** - konfiguracja

### Dodawanie danych

- **Oceny**:  Kliknij `+` w zakładce Oceny → wybierz przedmiot → wpisz ocenę
- **Zadania**: Kliknij `+` w zakładce Zadania → wpisz tytuł i termin
- **Nieobecności**: Automatycznie śledzone lub dodaj ręcznie

### Synchronizacja

Dane są automatycznie synchronizowane z chmurą Supabase po każdej zmianie.  Możesz korzystać z aplikacji na wielu urządzeniach.

## 📸 Zrzuty ekranu

| Ekran główny | Plan zajęć | Oceny |
|: ---:|:---:|:---:|
| ![Home](link_do_home.png) | ![Calendar](link_do_calendar.png) | ![Grades](link_do_grades.png) |

## ❓ FAQ

**Q: Czy aplikacja jest oficjalna?**  
A: Nie, to nieoficjalny projekt studencki dla UZ.

**Q:  Czy dane są bezpieczne?**  
A: Tak, dane są przechowywane lokalnie (Room) i opcjonalnie w Supabase z szyfrowaniem.

**Q:  Czy mogę używać offline? **  
A: Tak, wszystkie dane są dostępne offline.  Synchronizacja następuje gdy pojawi się internet.

**Q: Jak zaktualizować aplikację?**  
A: Pobierz nową wersję z zakładki Releases i zainstaluj na istniejącą. 

**Q: Gdzie zgłosić błąd?**  
A:  W zakładce [Issues](https://github.com/lifeoverthinker/my_uz_android/issues) tego repozytorium.

## 🔧 Rozwiązywanie problemów

### Aplikacja nie chce się zainstalować
- Upewnij się, że masz Androida 7.0+
- Włącz "Instalację z nieznanych źródeł" w ustawieniach

### Nie mogę się zalogować
- Sprawdź połączenie z internetem
- Upewnij się, że wprowadzasz poprawne dane UZ

### Dane się nie synchronizują
- Sprawdź połączenie z internetem
- Sprawdź ustawienia synchronizacji w aplikacji

---

<div align="center">

**Autor:** [@lifeoverthinker](https://github.com/lifeoverthinker)

Stworzone z ❤️ dla studentów UZ przy użyciu Kotlin & Jetpack Compose

⭐ Przydatna aplikacja?  Zostaw gwiazdkę!

</div>