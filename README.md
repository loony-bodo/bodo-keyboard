# Bodo Keyboard (बर' किब'र्ड)

A modern, fast, and lightweight Android Input Method (IME) for the Bodo language using the Devanagari script.

## 🚀 Features

- **Phonetic Transliteration:** Type Bodo using Roman characters (e.g., typing "aang" results in "आं").
- **Smart Learning:** The keyboard learns your typing style. When you pick a suggestion, it stores the Latin-to-Devanagari mapping in a local database for future accuracy.
- **Compose UI:** A fully modern interface built with Jetpack Compose.
- **Customizable:** 
    - Adjust keyboard height.
    - Toggle haptic feedback and keypress sounds.
    - Multiple modes: Bodo, English, Symbols, and Emojis.
- **In-App Preview:** A "Try It" section in the app to test the keyboard without leaving the application.
- **Setup Guide:** Interactive step-by-step setup to enable and select the keyboard.

## 🛠 Tech Stack

- **Kotlin:** 100% Kotlin codebase.
- **Jetpack Compose:** For the entire keyboard UI and the companion app.
- **SQLite:** To store user-learned transliteration rules.
- **InputMethodService:** The core Android API for custom keyboards.

## ⌨️ How to Use Transliteration

The transliteration engine follows standard phonetic rules for Bodo:
- **Consonants:** `k` -> `ख`, `ko` -> `क`, `g` -> `ग`, `ng` -> `ङ`, etc.
- **Vowels:** `a` -> `आ`, `i` -> `इ`, `w` -> `ओ`, `o` -> inherent vowel/`अ`.
- **Special:** `ng` at the end of a word automatically handles Anusvara/G-conjunct logic.

## 📥 Installation & Setup

1. **Build the APK:** Open the project in Android Studio and run `app`.
2. **Enable Keyboard:** Open the Bodo Keyboard app and follow **Step 1** to enable "Bodo Keyboard" in System Settings.
3. **Select Keyboard:** Follow **Step 2** to select "Bodo Keyboard" as your active input method.
4. **Start Typing:** Open any app (WhatsApp, Notes, etc.) and start typing in Bodo!

## 🤝 Contributing

Feel free to fork this project and submit pull requests for any features or bug fixes. For major changes, please open an issue first to discuss what you would like to change.

---
Developed with ❤️ for the Bodo Community.
