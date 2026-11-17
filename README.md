# Crypto SL - Android signal app (Kotlin + Jetpack Compose)

## APK හදාගන්නා ආකාරය
1. Android Studio (Koala හෝ අලුත්) install කරන්න.
2. File > Open > `SLCryptoSignal` folder එක තෝරන්න. Gradle sync වෙනකම් ඉන්න (internet ඕන).
3. Phone එක USB debugging දාලා Run ▶ ඔබන්න, නැත්නම්
   Build > Build Bundle(s) / APK(s) > Build APK(s).
   APK එක: `app/build/outputs/apk/debug/app-debug.apk`

## දැනට තියෙන දේ
- Signals list (Buy, Entry / Current / Stop Loss, Price move %)
- VIP locked signals (subscribe නොකළොත් ••••), tap කළාම Premium screen
- Signal details: leverage, max risk, price position, TP1-TP5 table, copy buttons
- Premium plans (Weekly / Monthly / Yearly LKR)
- Performance + Profile tabs
- Current price: Binance public API එකෙන් තත්පර 10කට වරක් live update (key ඕන නෑ)

## තවම නැති දේ (ඔබට / ඊළඟට)
- Signals දැනට `Data.kt` එකේ `sampleSignals` (demo). Firebase/server එකකින් ගන්න වෙනස් කරන්න ඕන.
- Google Play Billing සම්බන්ධ කරලා නෑ (`Ui.kt` > PremiumScreen TODO).
- USDT payment link එක TODO.
- Login / user accounts නෑ (නම "samantha" hard-coded).
- Performance numbers sample ඒවා.
- App icon default.

සටහන: Signals profit guarantee නෑ - app එකේ risk warning තියෙනවා, ඒක අයින් කරන්න එපා.
