# V380 Animal Guard Pro 38 - Field Tuned Readable Manual

## Защо има нова версия

От screenshot-а се виждаха две неща:

1. UI все още имаше бели карти с почти невидим текст на част от телефона.
2. Имаше boar 99% решение при кадър, който изглежда празен или без ясно прасе.

Pro36 поправя и двете.

## UI поправка

UI вече е изграден с plain high-contrast views, не с MaterialCard default цветове. Това трябва да премахне белите карти с бял текст.

## Детекция поправка

CameraBoarVision е направен по-строг:

- по-тесен ROI в реалната зона на движение
- игнорира яркия десен храст
- отхвърля border blobs
- изисква body core
- изисква realistic side/rear boar silhouette
- отхвърля empty field texture
- `camera-strong-boar-signature` е нужен за camera-only boar trigger

## Автоматично плашене

Остава включено:

- camera S01 siren
- phone fullscreen alarm
- alarm sound
- vibration
- reminders until ACK/Snooze

## Първи тест

1. Инсталирай Pro36.
2. Отвори приложението.
3. Натисни TEST FULL FIELD ALARM.
4. Провери камера сирена + телефон аларма.
5. Натисни Backtest -> RUN AI BACKTEST.
6. Натисни START GUARD.

## Ако пак има false positive

Отвори screenshot-а на event-а и ми прати:
- снимката
- Last decision box
- дали е имало реално прасе или не

След това ще направя Pro37 с още по-точно ROI/threshold tuning.
