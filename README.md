<div align="center">

<img src="https://via.placeholder.com/1200x400/1E1E1E/FFFFFF?text=My+UZ+-+Zaprojektowane+z+my%C5%9Bl%C4%85+o+Studentach" alt="My UZ Banner" width="100%">

# 🎓 My UZ - Twój Studencki Asystent

**Nowoczesna, przejrzysta i intuicyjna aplikacja dla studentów, zaprojektowana z naciskiem na najlepsze doświadczenie użytkownika (UX) i nowoczesny interfejs (UI).**

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-B125EA?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose-UI-4285F4?style=for-the-badge&logo=android)](https://developer.android.com/jetpack/compose)
[![UI/UX](https://img.shields.io/badge/UI%2FUX-Designed-FF61F6?style=for-the-badge&logo=figma)](#)

</div>

---

## 🎨 Wizja Projektowa (Design & UX)

Jako początkująca projektantka UI/UX, moim głównym celem przy tworzeniu **My UZ** było rozwiązanie problemu skomplikowanych i przestarzałych systemów uczelnianych. Zależało mi na stworzeniu narzędzia, które nie przytłacza, a pomaga zorganizować studenckie życie.

### 💡 Główne założenia:
- **Clean & Minimal UI:** Pozbycie się zbędnego szumu informacyjnego. Interfejs opiera się na kartach i wyraźnej hierarchii wizualnej.
- **Typografia:** Zastosowanie rodziny fontów **Inter** we wszystkich wariantach (od Thin do Black). Zapewnia to maksymalną czytelność na małych ekranach smartfonów, a jednocześnie nadaje aplikacji nowoczesny, technologiczny sznyt.
- **Wydajność wizualna:** Zamiast ciężkich obrazków (PNG/JPG), aplikacja wykorzystuje lekkie ilustracje wektorowe (SVG/XML). Dzięki temu grafiki (np. w stanach pustych / "empty states") są ostre jak brzytwa na każdym wyświetlaczu, a aplikacja zajmuje mniej miejsca w pamięci telefonu.
- **User-Centric:** Najważniejsze funkcje studenckie – plan zajęć, oceny i limity nieobecności – zostały zaprojektowane tak, aby były dostępne za pomocą maksymalnie 1-2 kliknięć.

---

## ✨ Funkcjonalności

Aplikacja zbiera wszystkie niezbędne dla studenta narzędzia w jednym, spójnym wizualnie ekosystemie:

- **📅 Plan zajęć:** Interaktywny kalendarz z intuicyjnym podglądem na nadchodzące ćwiczenia i wykłady.
- **📊 Oceny i statystyki:** Przejrzysty moduł śledzenia postępów z automatycznym kalkulatorem średniej.
- **🛑 Nieobecności:** Łatwe monitorowanie limitów nieobecności (żeby zawsze wiedzieć, czy można jeszcze pospać!).
- **✅ Zadania (To-Do):** Wbudowany menedżer zadań powiązany z konkretnymi zajęciami.
- **📱 Widgety na ekran domowy:** Błyskawiczny dostęp do dzisiejszego planu prosto z pulpitu telefonu.
- **☁️ Kopia zapasowa:** Synchronizacja z chmurą (Supabase), aby nigdy nie stracić swoich danych.

---

## 🛠️ Stos Technologiczny

Aplikacja to nie tylko design, ale też solidna inżynieria napisana zgodnie z najnowszymi standardami Androida:

* **Język:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architektura:** MVVM (Model-View-ViewModel) + Coroutines & Flow
* **Bazy danych:** Room Database (Lokalnie) + Supabase (Backend/Chmura)
* **Nawigacja:** Compose Navigation

---

## 🚀 Jak uruchomić projekt lokalnie?

1. Sklonuj repozytorium:
   ```bash
   git clone [https://github.com/lifeoverthinker/my_uz.git](https://github.com/lifeoverthinker/my_uz.git)
