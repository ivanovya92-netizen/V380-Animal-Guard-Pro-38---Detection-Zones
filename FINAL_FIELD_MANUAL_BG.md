# V380 Animal Guard Pro 38 - Final Field Build Manual

## 1. Какво прави тази версия

Това е финална field build версия за текущия етап.

При засечено прасе, диво прасе или boar-like unknown night object приложението автоматично прави:

1. Пуска вградената V380 сирена на камерата чрез доказания S01 direct protocol.
2. Пуска full-screen аларма на телефона.
3. Звъни с alarm ringtone.
4. Вибрира.
5. Показва persistent BOAR ALARM notification.
6. Повтаря reminder-и, докато не натиснеш ACK или Snooze.

## 2. Запазени критични неща

- S01 direct camera siren model е запазен 1:1 от работещия модел.
- Accessibility не се използва.
- V380 UI tap не се използва.
- CameraBoarVision exact camera profile е запазен.
- Right bright bush зоната остава игнорирана.
- Детекцията е насочена към долната лява и долната средна зона на кадъра.

## 3. Build в GitHub

Качи съдържанието на GitHubSafePack в repo root:

- `V380AnimalGuardPro34_Source.zip`
- `README_BG.md`
- `.github/workflows/build-release.yml`

После:

1. GitHub -> Actions
2. Build V380 Animal Guard Pro 38 Release APK
3. Run workflow
4. Download artifact: `V380AnimalGuardPro34-release-apk`
5. Install APK на телефона

## 4. Permissions на телефона

След install разреши:

1. Notifications
2. Alarms and reminders / exact alarms
3. Display over lock screen / full screen notifications
4. Battery unrestricted / no battery optimization
5. Background activity allowed
6. Autostart, ако OnePlus/OxygenOS го предлага

Ако full-screen не излиза, отвори App Info за приложението и провери notification category `BOAR ALARM - fullscreen`.

## 5. Първи тестове

След install направи точно това:

### Test 1 - Full field alarm

1. Open app
2. Home
3. TEST FULL FIELD ALARM

Очакван резултат:

- телефонът отваря full-screen alarm
- телефонът звъни
- телефонът вибрира
- камерата пуска S01 сирената

### Test 2 - Camera siren only

1. Home
2. TEST SIREN NOW

Очакван резултат:

- камерата пуска сирената

### Test 3 - Phone alarm only

1. Home
2. TEST PHONE ALARM

Очакван резултат:

- само телефонът алармира

### Test 4 - AI backtest

1. Backtest
2. RUN AI BACKTEST

Очакван резултат:

- PASS
- BoarVision AI 100%
- Siren policy 100%
- Safety 23/23

### Test 5 - Cloud check

1. Guard
2. CLOUD TEST ONCE

Очакван резултат:

- виждаш Cloud OK или последен event ID

## 6. Реална работа на имота

1. Отвори приложението.
2. Натисни START GUARD.
3. Остави телефона с интернет и без battery restriction.
4. При real V380 event приложението проверява snapshot.
5. Ако CameraBoarVision реши `boar` или `unknown boar-like night`, се пуска:
   - камера сирена
   - телефонна аларма
   - reminders

## 7. Какво да ми върнеш при проблем

Ако build fail:

- качи GitHub Actions log ZIP

Ако камерата не пусне сирена:

- кажи Last siren status
- качи PCAPdroid capture от приложението

Ако телефонът не звъни full-screen:

- кажи дали има notification
- кажи дали звук/вибрация има
- прати screenshot от App Info -> Notifications

Ако не разпознае прасе:

- Review -> MARK LAST AS BOAR / WILD BOAR
- Copy learning dataset
- прати ми dataset текста и снимката
